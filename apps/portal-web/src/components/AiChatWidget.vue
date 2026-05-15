<template>
  <button class="ai-fab" type="button" @click="openChat">{{ t('ai.open') }}</button>

  <section v-if="opened" class="ai-panel" aria-live="polite">
    <header class="ai-panel__header">
      <div>
        <p>{{ t('ai.eyebrow') }}</p>
        <strong>{{ t('ai.title') }}</strong>
      </div>
      <button type="button" :aria-label="t('ai.close')" @click="opened = false">×</button>
    </header>

    <div class="ai-panel__body">
      <aside class="ai-panel__history">
        <button class="ai-history-new" type="button" @click="startNew">{{ t('ai.newChat') }}</button>
        <div
          v-for="conversation in conversations"
          :key="conversation.id"
          class="ai-history-row"
          :class="{ 'is-active': activeConversationId === conversation.id }"
        >
          <button class="ai-history-item" type="button" @click="selectConversation(conversation.id)">
            {{ conversation.title }}
          </button>
          <button
            class="ai-history-delete"
            type="button"
            :title="t('ai.deleteChat')"
            :aria-label="t('ai.deleteChat')"
            @click="removeConversation(conversation.id)"
          >
            ×
          </button>
        </div>
      </aside>

      <main class="ai-panel__chat">
        <div ref="messageListRef" class="ai-messages">
          <p v-if="!messages.length" class="ai-empty">{{ t('ai.empty') }}</p>
          <article v-for="message in messages" :key="message.localId" :class="['ai-message', `ai-message--${message.role}`]">
            <span>{{ message.role === 'user' ? t('ai.you') : t('ai.assistant') }}</span>
            <div class="ai-message__bubble">
              <div
                v-if="message.role === 'assistant' && message.content"
                class="ai-message__rich"
                v-html="renderAiMessageHtml(message.content)"
              ></div>
              <div v-else-if="message.role === 'assistant'" class="ai-message__typing" aria-live="polite">
                <span></span>
                <span></span>
                <span></span>
                <em>{{ t('ai.sending') }}</em>
              </div>
              <p v-else>{{ message.content }}</p>
            </div>
          </article>
        </div>
        <p v-if="error" class="ai-error">{{ error }}</p>
        <form class="ai-input" @submit.prevent="send">
          <textarea
            v-model="draft"
            rows="2"
            enterkeyhint="send"
            :placeholder="t('ai.placeholder')"
            :disabled="streaming"
            @keydown="handleInputKeydown"
          ></textarea>
          <button type="submit" :disabled="streaming || !draft.trim()">
            {{ streaming ? t('ai.sending') : t('ai.send') }}
          </button>
        </form>
      </main>
    </div>
  </section>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  deleteAiConversation,
  fetchAiConversations,
  fetchAiMessages,
  streamAiChat,
  type AiConversation,
  type EntityId
} from '../api/portal'
import { useAuthStore } from '../stores/auth'
import { useLanguageStore } from '../stores/language'
import { renderAiMessageHtml } from '../utils/richText'

interface UiMessage {
  localId: string
  id?: EntityId
  role: 'user' | 'assistant'
  content: string
}

const router = useRouter()
const auth = useAuthStore()
const language = useLanguageStore()
const t = language.t
const opened = ref(false)
const conversations = ref<AiConversation[]>([])
const activeConversationId = ref<EntityId | null>(null)
const messages = ref<UiMessage[]>([])
const draft = ref('')
const streaming = ref(false)
const error = ref('')
const messageListRef = ref<HTMLElement | null>(null)

const openChat = async () => {
  if (!(await auth.ensureSession())) {
    await router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  opened.value = true
  await loadConversations()
}

const loadConversations = async (silent = false) => {
  try {
    conversations.value = await fetchAiConversations()
  } catch (caught) {
    if (!silent) {
      error.value = errorMessage(caught)
    }
  }
}

const selectConversation = async (id: EntityId) => {
  activeConversationId.value = id
  error.value = ''
  try {
    const history = await fetchAiMessages(id)
    messages.value = history.map((item) => ({
      localId: String(item.id),
      id: item.id,
      role: item.role,
      content: item.content
    }))
    await scrollToBottom()
  } catch (caught) {
    error.value = errorMessage(caught)
  }
}

const startNew = () => {
  activeConversationId.value = null
  messages.value = []
  error.value = ''
}

const removeConversation = async (id: EntityId) => {
  if (!window.confirm(t('ai.deleteConfirm'))) {
    return
  }
  error.value = ''
  try {
    await deleteAiConversation(id)
    conversations.value = conversations.value.filter((conversation) => conversation.id !== id)
    if (activeConversationId.value === id) {
      startNew()
    }
  } catch (caught) {
    error.value = errorMessage(caught)
  }
}

const handleInputKeydown = (event: KeyboardEvent) => {
  if (event.key !== 'Enter' || event.shiftKey || event.isComposing) {
    return
  }
  event.preventDefault()
  void send()
}

const send = async () => {
  const question = draft.value.trim()
  if (!question || streaming.value) {
    return
  }
  draft.value = ''
  error.value = ''
  streaming.value = true
  const assistantMessage: UiMessage = {
    localId: `assistant-${Date.now()}`,
    role: 'assistant',
    content: ''
  }
  messages.value.push({ localId: `user-${Date.now()}`, role: 'user', content: question }, assistantMessage)
  await scrollToBottom()
  try {
    await streamAiChat(
      { conversationId: activeConversationId.value, message: question },
      {
        onDelta: async (content) => {
          assistantMessage.content += content
          await scrollToBottom()
        },
        onDone: async (done) => {
          activeConversationId.value = done.conversationId
          assistantMessage.id = done.messageId
          await loadConversations(true)
        },
        onError: (message) => {
          error.value = message
        }
      }
    )
  } catch (caught) {
    error.value = errorMessage(caught)
  } finally {
    streaming.value = false
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const errorMessage = (caught: unknown) =>
  (caught as any)?.response?.data?.detail || (caught as Error)?.message || t('common.operationFailed')
</script>

<style scoped>
.ai-fab {
  position: fixed;
  right: 18px;
  bottom: 18px;
  z-index: 80;
  min-width: 54px;
  min-height: 54px;
  border: 0;
  border-radius: 999px;
  background: #17212f;
  color: #fff8e8;
  cursor: pointer;
  font-weight: 900;
  box-shadow: 0 16px 40px rgba(23, 33, 47, 0.22);
}

.ai-panel {
  position: fixed;
  right: 18px;
  bottom: 84px;
  z-index: 90;
  width: min(760px, calc(100vw - 32px));
  height: min(620px, calc(100vh - 118px));
  display: grid;
  grid-template-rows: auto 1fr;
  overflow: hidden;
  border: 1px solid rgba(23, 33, 47, 0.12);
  border-radius: 22px;
  background: rgba(255, 253, 247, 0.98);
  box-shadow: 0 24px 70px rgba(23, 33, 47, 0.24);
}

.ai-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  background: #17212f;
  color: #fff8e8;
}

.ai-panel__header p {
  margin: 0 0 4px;
  color: rgba(255, 248, 232, 0.68);
  font-size: 0.72rem;
  font-weight: 900;
}

.ai-panel__header button {
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  color: #fff8e8;
  cursor: pointer;
  font-size: 1.1rem;
}

.ai-panel__body {
  min-height: 0;
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
}

.ai-panel__history {
  min-height: 0;
  display: grid;
  align-content: start;
  gap: 8px;
  overflow: auto;
  padding: 12px;
  border-right: 1px solid rgba(23, 33, 47, 0.08);
  background: rgba(23, 33, 47, 0.04);
}

.ai-history-new {
  min-height: 34px;
  border: 0;
  border-radius: 12px;
  cursor: pointer;
  text-align: left;
  font-weight: 800;
}

.ai-history-new {
  padding: 0 12px;
  background: #17212f;
  color: #fff8e8;
}

.ai-history-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 28px;
  align-items: stretch;
  overflow: hidden;
  border-radius: 12px;
  background: #fff;
}

.ai-history-item {
  min-height: 34px;
  padding: 8px 10px;
  border: 0;
  background: #fff;
  color: #526173;
  cursor: pointer;
  text-align: left;
  font-weight: 800;
}

.ai-history-delete {
  border: 0;
  background: transparent;
  color: #9aa4b2;
  cursor: pointer;
  font-size: 1rem;
}

.ai-history-row.is-active .ai-history-item {
  color: #17212f;
  box-shadow: inset 3px 0 0 #c45f38;
}

.ai-panel__chat {
  min-height: 0;
  display: grid;
  grid-template-rows: 1fr auto auto;
}

.ai-messages {
  min-height: 0;
  overflow: auto;
  padding: 16px;
}

.ai-empty {
  margin: 32px auto;
  max-width: 28ch;
  color: #687487;
  text-align: center;
  line-height: 1.6;
}

.ai-message {
  max-width: 82%;
  margin-bottom: 12px;
}

.ai-message span {
  display: block;
  margin-bottom: 4px;
  color: #687487;
  font-size: 0.72rem;
  font-weight: 900;
}

.ai-message__bubble {
  margin: 0;
  line-height: 1.65;
  padding: 12px 14px;
  border-radius: 14px;
  background: #eef2f7;
}

.ai-message__bubble p {
  margin: 0;
  white-space: pre-wrap;
}

.ai-message--user {
  margin-left: auto;
}

.ai-message--user .ai-message__bubble {
  background: #17212f;
  color: #fff8e8;
}

.ai-message__rich {
  display: grid;
  gap: 8px;
}

.ai-message__rich :deep(p) {
  margin: 0;
}

.ai-message__rich :deep(h3),
.ai-message__rich :deep(h4),
.ai-message__rich :deep(h5) {
  margin: 2px 0 0;
  color: #17212f;
  font-size: 0.98rem;
  line-height: 1.35;
}

.ai-message__rich :deep(ul),
.ai-message__rich :deep(ol) {
  display: grid;
  gap: 7px;
  margin: 0;
  padding-left: 1.25rem;
}

.ai-message__rich :deep(li) {
  padding-left: 2px;
}

.ai-message__rich :deep(strong) {
  color: #17212f;
  font-weight: 900;
}

.ai-message__rich :deep(code) {
  padding: 2px 5px;
  border-radius: 6px;
  background: rgba(23, 33, 47, 0.08);
  color: #a84d2f;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.88em;
}

.ai-message__typing {
  display: flex;
  align-items: center;
  gap: 7px;
  min-height: 26px;
  color: #526173;
  font-weight: 800;
}

.ai-message__typing span {
  width: 7px;
  height: 7px;
  margin: 0;
  border-radius: 50%;
  background: #c45f38;
  animation: aiTypingPulse 1s ease-in-out infinite;
}

.ai-message__typing span:nth-child(2) {
  animation-delay: 0.14s;
}

.ai-message__typing span:nth-child(3) {
  animation-delay: 0.28s;
}

.ai-message__typing em {
  margin-left: 2px;
  font-style: normal;
  font-size: 0.88rem;
}

@keyframes aiTypingPulse {
  0%,
  80%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }

  40% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

.ai-error {
  margin: 0 16px 10px;
  color: #a73737;
  font-size: 0.86rem;
  font-weight: 800;
}

.ai-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  padding: 12px;
  border-top: 1px solid rgba(23, 33, 47, 0.08);
}

.ai-input textarea {
  width: 100%;
  resize: none;
  border: 1px solid rgba(23, 33, 47, 0.12);
  border-radius: 14px;
  padding: 10px 12px;
  outline: none;
}

.ai-input button {
  min-width: 76px;
  border: 0;
  border-radius: 14px;
  background: #c45f38;
  color: #fff8e8;
  cursor: pointer;
  font-weight: 900;
}

.ai-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 720px) {
  .ai-panel {
    left: 12px;
    right: 12px;
    bottom: 78px;
    width: auto;
  }

  .ai-panel__body {
    grid-template-columns: 1fr;
  }

  .ai-panel__history {
    display: flex;
    min-height: 58px;
    overflow-x: auto;
    border-right: 0;
    border-bottom: 1px solid rgba(23, 33, 47, 0.08);
  }

  .ai-history-row {
    min-width: 130px;
  }
}
</style>
