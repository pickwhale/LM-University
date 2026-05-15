<template>
  <div class="workspace">
    <aside class="workspace__sidebar">
      <div class="brand-block">
        <p class="eyebrow">{{ t('admin.panel.eyebrow') }}</p>
        <h1>{{ t('admin.panel.title') }}</h1>
        <p>{{ t('admin.panel.subtitle') }}</p>
      </div>

      <button
        v-for="section in sections"
        :key="section.id"
        class="nav-button"
        :class="{ 'is-active': activeSection === section.id }"
        @click="activateSection(section.id)"
      >
        <div>
          <strong>{{ section.label }}</strong>
          <span>{{ section.caption }}</span>
        </div>
        <em v-if="section.badge !== null">{{ section.badge }}</em>
      </button>

      <div class="profile-card panel-soft">
        <p class="eyebrow">{{ t('common.signedIn') }}</p>
        <strong>{{ auth.me?.displayName || auth.me?.username }}</strong>
        <span>{{ auth.me?.role }}</span>
        <button class="ghost-button" @click="logout">{{ t('common.logout') }}</button>
      </div>
    </aside>

    <main class="workspace__main">
      <header class="workspace__header">
        <div>
          <p class="eyebrow">{{ activeMeta.label }}</p>
          <h2>{{ activeMeta.title }}</h2>
          <p>{{ activeMeta.description }}</p>
        </div>
        <div class="header-actions">
          <button class="ghost-button" :disabled="summaryLoading" @click="refreshAll">
            {{ summaryLoading ? t('common.refreshing') : t('common.refreshData') }}
          </button>
        </div>
      </header>

      <section v-if="activeSection === 'dashboard'" class="section-stack">
        <div class="metrics-grid">
          <article v-for="card in metricCards" :key="card.label" class="metric-card">
            <p>{{ card.label }}</p>
            <strong>{{ card.value }}</strong>
            <span>{{ card.note }}</span>
          </article>
        </div>

        <div class="split-grid">
          <section class="panel-soft insight-panel">
            <p class="eyebrow">{{ t('admin.dashboard.priorityQueue') }}</p>
            <h3>{{ t('admin.dashboard.attentionTitle') }}</h3>
            <ul class="insight-list">
              <li>
                <strong>{{ summary?.pendingUniversityApplicationCount ?? 0 }}</strong>
                {{ t('admin.dashboard.pendingUniversity') }}
              </li>
              <li>
                <strong>{{ summary?.pendingMajorApplicationCount ?? 0 }}</strong>
                {{ t('admin.dashboard.pendingMajor') }}
              </li>
              <li>
                <strong>{{ summary?.newsCount ?? 0 }}</strong>
                {{ t('admin.dashboard.newsAvailable') }}
              </li>
            </ul>
          </section>

          <section class="panel-soft quick-actions">
            <p class="eyebrow">{{ t('admin.dashboard.quickActions') }}</p>
            <h3>{{ t('admin.dashboard.nextTask') }}</h3>
            <div class="quick-actions__grid">
              <button class="action-tile" @click="openUniversityDialog()">
                <strong>{{ t('admin.dashboard.addUniversityTitle') }}</strong>
                <span>{{ t('admin.dashboard.addUniversityText') }}</span>
              </button>
              <button class="action-tile" @click="openMajorDialog()">
                <strong>{{ t('admin.dashboard.addMajorTitle') }}</strong>
                <span>{{ t('admin.dashboard.addMajorText') }}</span>
              </button>
              <button class="action-tile" @click="openNewsDialog()">
                <strong>{{ t('admin.dashboard.publishNewsTitle') }}</strong>
                <span>{{ t('admin.dashboard.publishNewsText') }}</span>
              </button>
              <button class="action-tile" @click="activateSection('applications')">
                <strong>{{ t('admin.dashboard.reviewApplicationsTitle') }}</strong>
                <span>{{ t('admin.dashboard.reviewApplicationsText') }}</span>
              </button>
            </div>
          </section>
        </div>
      </section>

      <section v-else-if="activeSection === 'students'" class="section-stack">
        <div class="toolbar panel-soft">
          <div class="toolbar__search">
            <input v-model="studentFilters.keyword" :placeholder="t('admin.placeholder.studentSearch')" @keyup.enter="loadStudents()" />
          </div>
          <div class="toolbar__actions">
            <button class="ghost-button" @click="loadStudents()">{{ t('common.search') }}</button>
            <button class="primary-button" @click="openStudentDialog()">{{ t('admin.action.addStudent') }}</button>
          </div>
        </div>

        <section class="data-card">
          <el-table v-loading="sectionLoading" :data="studentPage.items" :empty-text="t('admin.empty.students')">
            <el-table-column :label="t('admin.field.avatar')" width="100">
              <template #default="{ row }">
                <el-image
                  v-if="row.avatarPath"
                  class="table-thumb"
                  :src="imageUrl(row.avatarPath)"
                  :preview-src-list="[imageUrl(row.avatarPath)]"
                  preview-teleported
                  fit="cover"
                />
                <span v-else class="muted-text">{{ t('common.noImage') }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="studentNo" :label="t('admin.field.studentNo')" min-width="130" />
            <el-table-column prop="fullName" :label="t('admin.field.studentName')" min-width="140" />
            <el-table-column prop="gender" :label="t('admin.field.gender')" width="100" />
            <el-table-column prop="college" :label="t('admin.field.college')" min-width="160" />
            <el-table-column prop="contactNumber" :label="t('admin.field.contactNumber')" min-width="150" />
            <el-table-column :label="t('admin.field.score')" width="110">
              <template #default="{ row }">{{ row.score ?? '-' }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" :label="t('admin.table.createdAt')" min-width="170" />
            <el-table-column :label="t('common.actions')" width="190" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openStudentDialog(row)">{{ t('common.edit') }}</el-button>
                <el-button link type="danger" @click="removeStudent(row.id)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="studentFilters.page"
              :page-size="studentFilters.size"
              :total="studentPage.total"
              @current-change="onStudentPageChange"
            />
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'universities'" class="section-stack">
        <div class="toolbar panel-soft">
          <div class="toolbar__search">
            <input v-model="universityFilters.keyword" :placeholder="t('admin.placeholder.universitySearch')" />
          </div>
          <div class="toolbar__actions">
            <button class="ghost-button" @click="provinceDialogVisible = true">{{ t('admin.action.manageProvinces') }}</button>
            <button class="ghost-button" @click="loadUniversities()">{{ t('common.search') }}</button>
            <button class="primary-button" @click="openUniversityDialog()">{{ t('admin.action.addUniversity') }}</button>
          </div>
        </div>

        <section class="data-card">
          <el-table v-loading="sectionLoading" :data="universityPage.items" :empty-text="t('admin.empty.universities')">
            <el-table-column prop="name" :label="t('admin.table.university')" min-width="180" />
            <el-table-column :label="t('admin.table.images')" width="150">
              <template #default="{ row }">
                <div v-if="imagePaths(row.imagePath).length" class="table-images">
                  <el-image
                    v-for="(path, index) in imagePaths(row.imagePath).slice(0, 3)"
                    :key="`${row.id}-${path}-${index}`"
                    class="table-thumb"
                    :src="imageUrl(path)"
                    :preview-src-list="previewImageUrls(row.imagePath)"
                    preview-teleported
                    fit="cover"
                  />
                  <span v-if="imagePaths(row.imagePath).length > 3" class="image-count">
                    +{{ imagePaths(row.imagePath).length - 3 }}
                  </span>
                </div>
                <span v-else class="muted-text">{{ t('common.noImage') }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('admin.table.province')" min-width="120">
              <template #default="{ row }">{{ provinceName(row.provinceId) }}</template>
            </el-table-column>
            <el-table-column prop="institutionType" :label="t('admin.table.type')" min-width="140" />
            <el-table-column prop="keyness" :label="t('admin.table.tier')" min-width="120" />
            <el-table-column prop="phone" :label="t('admin.table.phone')" min-width="140" />
            <el-table-column prop="website" :label="t('admin.table.website')" min-width="220" />
            <el-table-column :label="t('common.actions')" width="190" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openUniversityDialog(row)">{{ t('common.edit') }}</el-button>
                <el-button link type="danger" @click="removeUniversity(row.id)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="universityFilters.page"
              :page-size="universityFilters.size"
              :total="universityPage.total"
              @current-change="onUniversityPageChange"
            />
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'majors'" class="section-stack">
        <div class="toolbar panel-soft">
          <div class="toolbar__search">
            <input v-model="majorFilters.keyword" :placeholder="t('admin.placeholder.majorSearch')" />
          </div>
          <div class="toolbar__actions">
            <button class="ghost-button" @click="loadMajors()">{{ t('common.search') }}</button>
            <button class="primary-button" @click="openMajorDialog()">{{ t('admin.action.addMajor') }}</button>
          </div>
        </div>

        <section class="data-card">
          <el-table v-loading="sectionLoading" :data="majorPage.items" :empty-text="t('admin.empty.majors')">
            <el-table-column prop="code" :label="t('admin.table.code')" width="120" />
            <el-table-column prop="name" :label="t('admin.table.major')" min-width="220" />
            <el-table-column :label="t('admin.table.cover')" width="150">
              <template #default="{ row }">
                <div v-if="imagePaths(row.coverPath).length" class="table-images">
                  <el-image
                    v-for="(path, index) in imagePaths(row.coverPath).slice(0, 3)"
                    :key="`${row.id}-${path}-${index}`"
                    class="table-thumb"
                    :src="imageUrl(path)"
                    :preview-src-list="previewImageUrls(row.coverPath)"
                    preview-teleported
                    fit="cover"
                  />
                  <span v-if="imagePaths(row.coverPath).length > 3" class="image-count">
                    +{{ imagePaths(row.coverPath).length - 3 }}
                  </span>
                </div>
                <span v-else class="muted-text">{{ t('common.noImage') }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('admin.table.university')" min-width="180">
              <template #default="{ row }">{{ universityName(row.universityId) }}</template>
            </el-table-column>
            <el-table-column prop="durationOfStudy" :label="t('admin.table.duration')" width="120" />
            <el-table-column prop="cutOffScore" :label="t('admin.table.cutOff')" width="110" />
            <el-table-column prop="enrollmentQuota" :label="t('admin.table.quota')" width="90" />
            <el-table-column :label="t('common.actions')" width="190" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openMajorDialog(row)">{{ t('common.edit') }}</el-button>
                <el-button link type="danger" @click="removeMajor(row.id)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="majorFilters.page"
              :page-size="majorFilters.size"
              :total="majorPage.total"
              @current-change="onMajorPageChange"
            />
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'applications'" class="section-stack">
        <section class="data-card tabs-card">
          <div class="tab-switcher">
            <button
              class="tab-button"
              :class="{ 'is-active': applicationTab === 'university' }"
              @click="switchApplicationTab('university')"
            >
              {{ t('admin.tab.universityApplications') }}
            </button>
            <button
              class="tab-button"
              :class="{ 'is-active': applicationTab === 'major' }"
              @click="switchApplicationTab('major')"
            >
              {{ t('admin.tab.majorApplications') }}
            </button>
          </div>

          <div class="toolbar toolbar--compact">
            <div class="toolbar__search toolbar__search--select">
              <el-select
                v-if="applicationTab === 'university'"
                v-model="applicationFilters.universityStatus"
                clearable
                :placeholder="t('admin.placeholder.statusFilter')"
              >
                <el-option :label="t('common.pending')" value="PENDING" />
                <el-option :label="t('common.approved')" value="APPROVED" />
                <el-option :label="t('common.rejected')" value="REJECTED" />
              </el-select>
              <el-select
                v-else
                v-model="applicationFilters.majorStatus"
                clearable
                :placeholder="t('admin.placeholder.statusFilter')"
              >
                <el-option :label="t('common.pending')" value="PENDING" />
                <el-option :label="t('common.approved')" value="APPROVED" />
                <el-option :label="t('common.rejected')" value="REJECTED" />
              </el-select>
            </div>
            <div class="toolbar__actions">
              <button class="ghost-button" @click="reloadApplications">{{ t('common.applyFilter') }}</button>
            </div>
          </div>

          <el-table
            v-if="applicationTab === 'university'"
            v-loading="sectionLoading"
            :data="universityApplicationPage.items"
            :empty-text="t('admin.empty.universityApplications')"
          >
            <el-table-column prop="registrationNo" :label="t('admin.table.registration')" width="160" />
            <el-table-column prop="studentNameSnapshot" :label="t('admin.table.student')" min-width="140" />
            <el-table-column prop="studentNoSnapshot" :label="t('admin.table.studentNo')" width="130" />
            <el-table-column prop="universityNameSnapshot" :label="t('admin.table.university')" min-width="180" />
            <el-table-column :label="t('common.status')" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewComment" :label="t('admin.table.comment')" min-width="220" />
            <el-table-column :label="t('common.actions')" width="170" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openReviewDialog('university', row)">{{ t('admin.action.review') }}</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-table
            v-else
            v-loading="sectionLoading"
            :data="majorApplicationPage.items"
            :empty-text="t('admin.empty.majorApplications')"
          >
            <el-table-column prop="studentNameSnapshot" :label="t('admin.table.student')" min-width="140" />
            <el-table-column prop="studentNoSnapshot" :label="t('admin.table.studentNo')" width="130" />
            <el-table-column prop="majorCodeSnapshot" :label="t('admin.table.majorCode')" width="130" />
            <el-table-column prop="majorNameSnapshot" :label="t('admin.table.major')" min-width="180" />
            <el-table-column prop="universityNameSnapshot" :label="t('admin.table.university')" min-width="180" />
            <el-table-column :label="t('common.status')" width="120">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reviewComment" :label="t('admin.table.comment')" min-width="220" />
            <el-table-column :label="t('common.actions')" width="170" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openReviewDialog('major', row)">{{ t('admin.action.review') }}</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="applicationTab === 'university' ? applicationFilters.universityPage : applicationFilters.majorPage"
              :page-size="applicationFilters.size"
              :total="applicationTab === 'university' ? universityApplicationPage.total : majorApplicationPage.total"
              @current-change="onApplicationPageChange"
            />
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'results'" class="section-stack">
        <section class="data-card tabs-card">
          <div class="tab-switcher">
            <button
              class="tab-button"
              :class="{ 'is-active': resultTab === 'admission' }"
              @click="switchResultTab('admission')"
            >
              {{ t('admin.tab.admissionResults') }}
            </button>
            <button
              class="tab-button"
              :class="{ 'is-active': resultTab === 'academic' }"
              @click="switchResultTab('academic')"
            >
              {{ t('admin.tab.academicResults') }}
            </button>
          </div>

          <div class="toolbar toolbar--compact">
            <div class="toolbar__search toolbar__search--static">
              <span>{{ t('admin.results.description') }}</span>
            </div>
            <div class="toolbar__actions">
              <button class="primary-button" @click="resultTab === 'admission' ? openAdmissionResultDialog() : openAcademicResultDialog()">
                {{ resultTab === 'admission' ? t('admin.action.addAdmissionResult') : t('admin.action.addAcademicResult') }}
              </button>
            </div>
          </div>

          <el-table
            v-if="resultTab === 'admission'"
            v-loading="sectionLoading"
            :data="admissionResultPage.items"
            :empty-text="t('admin.empty.admissionResults')"
          >
            <el-table-column prop="applicationId" :label="t('admin.table.applicationId')" width="140" />
            <el-table-column :label="t('admin.table.result')" width="130">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.resultStatus)">{{ statusLabel(row.resultStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="feedback" :label="t('admin.table.feedback')" min-width="260" show-overflow-tooltip />
            <el-table-column prop="feedbackAt" :label="t('admin.table.feedbackAt')" width="180" />
            <el-table-column :label="t('common.actions')" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openAdmissionResultDialog(row)">{{ t('common.edit') }}</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-table
            v-else
            v-loading="sectionLoading"
            :data="academicResultPage.items"
            :empty-text="t('admin.empty.academicResults')"
          >
            <el-table-column prop="studentId" :label="t('admin.table.studentId')" width="120" />
            <el-table-column prop="reportNo" :label="t('admin.table.reportNo')" width="160" />
            <el-table-column prop="grade" :label="t('admin.table.grade')" width="100" />
            <el-table-column prop="gradeEvaluation" :label="t('admin.table.evaluation')" min-width="160" />
            <el-table-column prop="reportContent" :label="t('admin.table.content')" min-width="260" show-overflow-tooltip />
            <el-table-column prop="enteredAt" :label="t('admin.table.enteredAt')" width="140" />
            <el-table-column :label="t('common.actions')" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openAcademicResultDialog(row)">{{ t('common.edit') }}</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="resultTab === 'admission' ? resultFilters.admissionPage : resultFilters.academicPage"
              :page-size="resultFilters.size"
              :total="resultTab === 'admission' ? admissionResultPage.total : academicResultPage.total"
              @current-change="onResultPageChange"
            />
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'consultations'" class="section-stack">
        <section class="data-card">
          <el-table v-loading="sectionLoading" :data="consultationPage.items" :empty-text="t('admin.empty.consultations')">
            <el-table-column prop="studentId" :label="t('admin.table.studentId')" width="120" />
            <el-table-column prop="question" :label="t('admin.table.question')" min-width="280" show-overflow-tooltip />
            <el-table-column prop="reply" :label="t('common.reply')" min-width="280" show-overflow-tooltip />
            <el-table-column :label="t('common.status')" width="110">
              <template #default="{ row }">
                <el-tag :type="row.replied ? 'success' : 'warning'">{{ row.replied ? t('common.replied') : t('common.pending') }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" :label="t('admin.table.createdAt')" width="180" />
            <el-table-column :label="t('common.actions')" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openConsultationDialog(row)">
                  {{ row.replied ? t('common.editReply') : t('common.reply') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="consultationFilters.page"
              :page-size="consultationFilters.size"
              :total="consultationPage.total"
              @current-change="onConsultationPageChange"
            />
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'news'" class="section-stack">
        <div class="toolbar panel-soft">
          <div class="toolbar__search">
            <input v-model="newsFilters.keyword" :placeholder="t('admin.placeholder.newsSearch')" />
          </div>
          <div class="toolbar__actions">
            <button class="ghost-button" @click="loadNews()">{{ t('common.search') }}</button>
            <button class="primary-button" @click="openNewsDialog()">{{ t('admin.action.addNews') }}</button>
          </div>
        </div>

        <section class="data-card">
          <el-table v-loading="sectionLoading" :data="newsPage.items" :empty-text="t('admin.empty.news')">
            <el-table-column prop="title" :label="t('admin.table.title')" min-width="220" />
            <el-table-column :label="t('admin.table.image')" width="150">
              <template #default="{ row }">
                <div v-if="imagePaths(row.picturePath).length" class="table-images">
                  <el-image
                    v-for="(path, index) in imagePaths(row.picturePath).slice(0, 3)"
                    :key="`${row.id}-${path}-${index}`"
                    class="table-thumb"
                    :src="imageUrl(path)"
                    :preview-src-list="previewImageUrls(row.picturePath)"
                    preview-teleported
                    fit="cover"
                  />
                </div>
                <span v-else class="muted-text">{{ t('common.noImage') }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="introduction" :label="t('admin.table.introduction')" min-width="260" show-overflow-tooltip />
            <el-table-column prop="publishedAt" :label="t('admin.table.publishedAt')" width="180" />
            <el-table-column :label="t('common.actions')" width="190" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openNewsDialog(row)">{{ t('common.edit') }}</el-button>
                <el-button link type="danger" @click="removeNews(row.id)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap">
            <el-pagination
              background
              layout="prev, pager, next"
              :current-page="newsFilters.page"
              :page-size="newsFilters.size"
              :total="newsPage.total"
              @current-change="onNewsPageChange"
            />
          </div>
        </section>
      </section>

      <section v-else-if="activeSection === 'pages'" class="section-stack">
        <div class="toolbar panel-soft">
          <div class="toolbar__search toolbar__search--static">
            <span>{{ t('admin.pages.description') }}</span>
          </div>
          <div class="toolbar__actions">
            <button class="primary-button" @click="openPageDialog()">{{ t('admin.action.addPage') }}</button>
          </div>
        </div>

        <section class="data-card">
          <el-table v-loading="sectionLoading" :data="pages" :empty-text="t('admin.empty.pages')">
            <el-table-column prop="slug" :label="t('admin.table.slug')" width="150" />
            <el-table-column prop="title" :label="t('admin.table.title')" min-width="220" />
            <el-table-column prop="subtitle" :label="t('admin.table.subtitle')" min-width="180" />
            <el-table-column :label="t('admin.table.images')" width="150">
              <template #default="{ row }">
                <div v-if="pageImagePaths(row).length" class="table-images">
                  <el-image
                    v-for="(path, index) in pageImagePaths(row).slice(0, 3)"
                    :key="`${row.id}-${path}-${index}`"
                    class="table-thumb"
                    :src="imageUrl(path)"
                    :preview-src-list="pagePreviewImageUrls(row)"
                    preview-teleported
                    fit="cover"
                  />
                </div>
                <span v-else class="muted-text">{{ t('common.noImage') }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="content" :label="t('admin.table.content')" min-width="260" show-overflow-tooltip />
            <el-table-column :label="t('common.actions')" width="190" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openPageDialog(row)">{{ t('common.edit') }}</el-button>
                <el-button link type="danger" @click="removePage(row.id)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </section>

      <section v-else-if="activeSection === 'settings'" class="section-stack">
        <div class="toolbar panel-soft">
          <div class="toolbar__search toolbar__search--static">
            <span>{{ t('admin.settings.description') }}</span>
          </div>
          <div class="toolbar__actions">
            <button class="primary-button" @click="openSettingDialog()">{{ t('admin.action.addSetting') }}</button>
          </div>
        </div>

        <section class="data-card">
          <el-table v-loading="sectionLoading" :data="settings" :empty-text="t('admin.empty.settings')">
            <el-table-column prop="settingKey" :label="t('admin.table.key')" min-width="220" />
            <el-table-column prop="settingValue" :label="t('admin.table.value')" min-width="260" show-overflow-tooltip />
            <el-table-column :label="t('common.actions')" width="190" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openSettingDialog(row)">{{ t('common.edit') }}</el-button>
                <el-button link type="danger" @click="removeSetting(row.id)">{{ t('common.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </section>

      <section v-else-if="activeSection === 'ai'" class="section-stack">
        <div class="toolbar panel-soft">
          <div class="toolbar__search toolbar__search--static">
            <span>{{ t('admin.ai.description') }}</span>
          </div>
          <div class="toolbar__actions">
            <button class="ghost-button" :disabled="sectionLoading" @click="loadAiConfig">{{ t('common.refreshData') }}</button>
          </div>
        </div>

        <section v-loading="sectionLoading" class="data-card ai-config-card">
          <div class="dialog-grid">
            <label class="field ai-switch-field">
              <span>{{ t('admin.ai.enabled') }}</span>
              <el-switch v-model="aiConfigForm.enabled" />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.providerName') }}</span>
              <input v-model="aiConfigForm.providerName" />
            </label>
            <label class="field field--full">
              <span>{{ t('admin.ai.endpointUrl') }}</span>
              <input v-model="aiConfigForm.endpointUrl" placeholder="https://api.example.com/v1/chat/stream" />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.httpMethod') }}</span>
              <input v-model="aiConfigForm.httpMethod" disabled />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.streamProtocol') }}</span>
              <el-select v-model="aiConfigForm.streamProtocol">
                <el-option label="AUTO" value="AUTO" />
                <el-option label="SSE" value="SSE" />
                <el-option label="TEXT" value="TEXT" />
              </el-select>
            </label>
            <label class="field">
              <span>{{ t('admin.ai.model') }}</span>
              <input v-model="aiConfigForm.model" />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.apiKey') }}</span>
              <input v-model="aiConfigForm.apiKey" type="password" :placeholder="aiConfig?.apiKeySet ? t('admin.ai.keepApiKey') : t('admin.ai.apiKeyPlaceholder')" />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.temperature') }}</span>
              <el-input-number v-model="aiConfigForm.temperature" :min="0" :max="2" :step="0.1" />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.maxTokens') }}</span>
              <el-input-number v-model="aiConfigForm.maxTokens" :min="1" :max="200000" :step="128" />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.responseTextPath') }}</span>
              <input v-model="aiConfigForm.responseTextPath" placeholder="choices.0.delta.content" />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.doneMarker') }}</span>
              <input v-model="aiConfigForm.doneMarker" />
            </label>
            <label class="field">
              <span>{{ t('admin.ai.timeoutSeconds') }}</span>
              <el-input-number v-model="aiConfigForm.timeoutSeconds" :min="3" :max="300" />
            </label>
            <label class="field field--full">
              <span>{{ t('admin.ai.headersTemplate') }}</span>
              <textarea v-model="aiConfigForm.headersTemplate" rows="5" spellcheck="false" />
            </label>
            <label class="field field--full">
              <span>{{ t('admin.ai.bodyTemplate') }}</span>
              <textarea v-model="aiConfigForm.bodyTemplate" rows="12" spellcheck="false" />
            </label>
            <label class="field field--full">
              <span>{{ t('admin.ai.systemPrompt') }}</span>
              <textarea v-model="aiConfigForm.systemPrompt" rows="5" />
            </label>
            <label class="field field--full">
              <span>{{ t('admin.ai.testMessage') }}</span>
              <textarea v-model="aiTestMessage" rows="3" />
            </label>
          </div>

          <p class="ai-secret-note">
            {{ aiConfig?.apiKeySet ? t('admin.ai.apiKeySet') : t('admin.ai.apiKeyPlaceholder') }}
          </p>

          <div class="ai-config-actions">
            <button class="primary-button" :disabled="saving" @click="saveAiConfig()">
              {{ saving ? t('admin.action.saving') : t('admin.ai.save') }}
            </button>
            <button class="ghost-button" :disabled="saving || aiTesting" @click="runAiConfigTest">
              {{ aiTesting ? t('admin.ai.testing') : t('admin.ai.test') }}
            </button>
          </div>

          <div v-if="aiTestResult" class="ai-test-result">
            <strong>{{ t('admin.ai.testResult') }}</strong>
            <p>{{ aiTestResult }}</p>
            <pre v-if="aiTestPreview">{{ aiTestPreview }}</pre>
          </div>
        </section>
      </section>
    </main>

    <el-dialog v-model="provinceDialogVisible" :title="t('admin.dialog.manageProvinces')" width="640px">
      <div class="dialog-toolbar">
        <input v-model="provinceForm.name" :placeholder="t('admin.placeholder.provinceName')" />
        <button class="primary-button" @click="saveProvince">
          {{ provinceForm.id ? t('admin.action.updateProvince') : t('admin.action.addProvince') }}
        </button>
      </div>
      <el-table :data="provinces" max-height="360">
        <el-table-column prop="name" :label="t('admin.table.province')" />
        <el-table-column :label="t('common.actions')" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="editProvince(row)">{{ t('common.edit') }}</el-button>
            <el-button link type="danger" @click="removeProvince(row.id)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="studentDialogVisible" :title="studentForm.id ? t('admin.dialog.editStudent') : t('admin.dialog.addStudent')" width="720px">
      <div class="dialog-grid">
        <label class="field">
          <span>{{ t('admin.field.studentNo') }}</span>
          <input v-model="studentForm.studentNo" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.password') }}</span>
          <input v-model="studentForm.password" type="password" :placeholder="studentForm.id ? t('admin.placeholder.studentPassword') : ''" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.studentName') }}</span>
          <input v-model="studentForm.fullName" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.gender') }}</span>
          <input v-model="studentForm.gender" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.college') }}</span>
          <input v-model="studentForm.college" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.contactNumber') }}</span>
          <input v-model="studentForm.contactNumber" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.score') }}</span>
          <input v-model.number="studentForm.score" type="number" min="0" step="0.1" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.avatar') }}</span>
          <el-upload
            class="image-uploader"
            accept="image/*"
            :show-file-list="false"
            :http-request="uploadStudentAvatar"
          >
            <button class="upload-button" type="button" :disabled="imageUploading.student">
              {{ imageUploading.student ? t('common.uploading') : t('admin.action.uploadImage') }}
            </button>
          </el-upload>
          <div v-if="studentForm.avatarPath" class="form-images">
            <div class="form-image-item">
              <el-image
                class="form-thumb"
                :src="imageUrl(studentForm.avatarPath)"
                :preview-src-list="[imageUrl(studentForm.avatarPath)]"
                preview-teleported
                fit="cover"
              />
              <button class="image-remove" type="button" @click="studentForm.avatarPath = ''">{{ t('common.remove') }}</button>
            </div>
          </div>
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="studentDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="saveStudent">
          {{ saving ? t('admin.action.saving') : t('admin.action.saveStudent') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="universityDialogVisible" :title="universityForm.id ? t('admin.dialog.editUniversity') : t('admin.dialog.addUniversity')" width="760px">
      <div class="dialog-grid">
        <label class="field">
          <span>{{ t('admin.field.name') }}</span>
          <input v-model="universityForm.name" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.province') }}</span>
          <el-select v-model="universityForm.provinceId" :placeholder="t('admin.placeholder.selectProvince')">
            <el-option v-for="province in provinces" :key="province.id" :label="province.name" :value="province.id" />
          </el-select>
        </label>
        <label class="field">
          <span>{{ t('admin.field.institutionType') }}</span>
          <input v-model="universityForm.institutionType" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.tier') }}</span>
          <input v-model="universityForm.keyness" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.phone') }}</span>
          <input v-model="universityForm.phone" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.website') }}</span>
          <input v-model="universityForm.website" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.universityImages') }}</span>
          <el-upload
            class="image-uploader"
            accept="image/*"
            :show-file-list="false"
            :http-request="uploadUniversityImage"
          >
            <button class="upload-button" type="button" :disabled="imageUploading.university">
              {{ imageUploading.university ? t('common.uploading') : t('admin.action.uploadImage') }}
            </button>
          </el-upload>
          <div v-if="imagePaths(universityForm.imagePath).length" class="form-images">
            <div v-for="(path, index) in imagePaths(universityForm.imagePath)" :key="`university-image-${path}-${index}`" class="form-image-item">
              <el-image
                class="form-thumb"
                :src="imageUrl(path)"
                :preview-src-list="previewImageUrls(universityForm.imagePath)"
                preview-teleported
                fit="cover"
              />
              <button class="image-remove" type="button" @click="removeUniversityImage(path)">{{ t('common.remove') }}</button>
            </div>
          </div>
        </label>
        <label class="field field--full">
          <span>{{ t('admin.table.introduction') }}</span>
          <textarea v-model="universityForm.introduction" rows="5" />
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="universityDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="saveUniversity">
          {{ saving ? t('admin.action.saving') : t('admin.action.saveUniversity') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="majorDialogVisible" :title="majorForm.id ? t('admin.dialog.editMajor') : t('admin.dialog.addMajor')" width="760px">
      <div class="dialog-grid">
        <label class="field">
          <span>{{ t('admin.field.majorCode') }}</span>
          <input v-model="majorForm.code" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.university') }}</span>
          <el-select v-model="majorForm.universityId" :placeholder="t('admin.placeholder.selectUniversity')">
            <el-option v-for="option in universityOptions" :key="option.id" :label="option.name" :value="option.id" />
          </el-select>
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.majorName') }}</span>
          <input v-model="majorForm.name" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.duration') }}</span>
          <input v-model="majorForm.durationOfStudy" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.cutOffScore') }}</span>
          <input v-model="majorForm.cutOffScore" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.enrollmentQuota') }}</span>
          <input v-model.number="majorForm.enrollmentQuota" type="number" min="0" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.majorCoverImages') }}</span>
          <el-upload
            class="image-uploader"
            accept="image/*"
            :show-file-list="false"
            :http-request="uploadMajorCover"
          >
            <button class="upload-button" type="button" :disabled="imageUploading.major">
              {{ imageUploading.major ? t('common.uploading') : t('admin.action.uploadCover') }}
            </button>
          </el-upload>
          <div v-if="imagePaths(majorForm.coverPath).length" class="form-images">
            <div v-for="(path, index) in imagePaths(majorForm.coverPath)" :key="`major-cover-${path}-${index}`" class="form-image-item">
              <el-image
                class="form-thumb"
                :src="imageUrl(path)"
                :preview-src-list="previewImageUrls(majorForm.coverPath)"
                preview-teleported
                fit="cover"
              />
              <button class="image-remove" type="button" @click="removeMajorCover(path)">{{ t('common.remove') }}</button>
            </div>
          </div>
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.curriculum') }}</span>
          <textarea v-model="majorForm.curriculum" rows="5" />
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="majorDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="saveMajor">
          {{ saving ? t('admin.action.saving') : t('admin.action.saveMajor') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="newsDialogVisible" :title="newsForm.id ? t('admin.dialog.editNews') : t('admin.dialog.publishNews')" width="760px">
      <div class="dialog-grid">
        <label class="field field--full">
          <span>{{ t('admin.table.title') }}</span>
          <input v-model="newsForm.title" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.table.introduction') }}</span>
          <textarea v-model="newsForm.introduction" rows="3" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.table.content') }}</span>
          <textarea v-model="newsForm.content" rows="8" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.publishedAt') }}</span>
          <el-date-picker
            v-model="newsForm.publishedAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :placeholder="t('admin.placeholder.selectDateTime')"
          />
        </label>
        <label class="field">
          <span>{{ t('admin.field.newsPicture') }}</span>
          <el-upload
            class="image-uploader"
            accept="image/*"
            :show-file-list="false"
            :http-request="uploadNewsPicture"
          >
            <button class="upload-button" type="button" :disabled="imageUploading.news">
              {{ imageUploading.news ? t('common.uploading') : t('admin.action.uploadPicture') }}
            </button>
          </el-upload>
          <div v-if="imagePaths(newsForm.picturePath).length" class="form-images">
            <div v-for="(path, index) in imagePaths(newsForm.picturePath)" :key="`news-picture-${path}-${index}`" class="form-image-item">
              <el-image
                class="form-thumb"
                :src="imageUrl(path)"
                :preview-src-list="previewImageUrls(newsForm.picturePath)"
                preview-teleported
                fit="cover"
              />
              <button class="image-remove" type="button" @click="removeNewsPicture(path)">{{ t('common.remove') }}</button>
            </div>
          </div>
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="newsDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="saveNews">
          {{ saving ? t('admin.action.saving') : t('admin.action.saveNews') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="pageDialogVisible" :title="pageForm.id ? t('admin.dialog.editPage') : t('admin.dialog.addPage')" width="780px">
      <div class="dialog-grid">
        <label class="field">
          <span>{{ t('admin.table.slug') }}</span>
          <input v-model="pageForm.slug" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.title') }}</span>
          <input v-model="pageForm.title" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.table.subtitle') }}</span>
          <input v-model="pageForm.subtitle" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.table.content') }}</span>
          <textarea v-model="pageForm.content" rows="8" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.picture1') }}</span>
          <el-upload class="image-uploader" accept="image/*" :show-file-list="false" :http-request="uploadPagePicture1">
            <button class="upload-button" type="button" :disabled="imageUploading.page1">
              {{ imageUploading.page1 ? t('common.uploading') : t('admin.field.picture1') }}
            </button>
          </el-upload>
          <div v-if="imagePaths(pageForm.picture1Path).length" class="form-images">
            <div v-for="(path, index) in imagePaths(pageForm.picture1Path)" :key="`page-picture-1-${path}-${index}`" class="form-image-item">
              <el-image
                class="form-thumb"
                :src="imageUrl(path)"
                :preview-src-list="previewImageUrls(pageForm.picture1Path)"
                preview-teleported
                fit="cover"
              />
              <button class="image-remove" type="button" @click="pageForm.picture1Path = ''">{{ t('common.remove') }}</button>
            </div>
          </div>
        </label>
        <label class="field">
          <span>{{ t('admin.field.picture2') }}</span>
          <el-upload class="image-uploader" accept="image/*" :show-file-list="false" :http-request="uploadPagePicture2">
            <button class="upload-button" type="button" :disabled="imageUploading.page2">
              {{ imageUploading.page2 ? t('common.uploading') : t('admin.field.picture2') }}
            </button>
          </el-upload>
          <div v-if="imagePaths(pageForm.picture2Path).length" class="form-images">
            <div v-for="(path, index) in imagePaths(pageForm.picture2Path)" :key="`page-picture-2-${path}-${index}`" class="form-image-item">
              <el-image
                class="form-thumb"
                :src="imageUrl(path)"
                :preview-src-list="previewImageUrls(pageForm.picture2Path)"
                preview-teleported
                fit="cover"
              />
              <button class="image-remove" type="button" @click="pageForm.picture2Path = ''">{{ t('common.remove') }}</button>
            </div>
          </div>
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.picture3') }}</span>
          <el-upload class="image-uploader" accept="image/*" :show-file-list="false" :http-request="uploadPagePicture3">
            <button class="upload-button" type="button" :disabled="imageUploading.page3">
              {{ imageUploading.page3 ? t('common.uploading') : t('admin.field.picture3') }}
            </button>
          </el-upload>
          <div v-if="imagePaths(pageForm.picture3Path).length" class="form-images">
            <div v-for="(path, index) in imagePaths(pageForm.picture3Path)" :key="`page-picture-3-${path}-${index}`" class="form-image-item">
              <el-image
                class="form-thumb"
                :src="imageUrl(path)"
                :preview-src-list="previewImageUrls(pageForm.picture3Path)"
                preview-teleported
                fit="cover"
              />
              <button class="image-remove" type="button" @click="pageForm.picture3Path = ''">{{ t('common.remove') }}</button>
            </div>
          </div>
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="pageDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="savePage">
          {{ saving ? t('admin.action.saving') : t('admin.action.savePage') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="settingDialogVisible" :title="settingForm.id ? t('admin.dialog.editSetting') : t('admin.dialog.addSetting')" width="680px">
      <div class="dialog-grid">
        <label class="field field--full">
          <span>{{ t('admin.field.settingKey') }}</span>
          <input v-model="settingForm.settingKey" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.settingValue') }}</span>
          <textarea v-model="settingForm.settingValue" rows="6" />
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="settingDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="saveSetting">
          {{ saving ? t('admin.action.saving') : t('admin.action.saveSetting') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="admissionResultDialogVisible" :title="admissionResultForm.id ? t('admin.dialog.editAdmissionResult') : t('admin.dialog.addAdmissionResult')" width="680px">
      <div class="dialog-grid">
        <label class="field">
          <span>{{ t('admin.table.applicationId') }}</span>
          <input v-model.number="admissionResultForm.applicationId" type="number" min="1" />
        </label>
        <label class="field">
          <span>{{ t('admin.field.resultStatus') }}</span>
          <el-select v-model="admissionResultForm.resultStatus" :placeholder="t('admin.placeholder.selectResult')">
            <el-option :label="t('common.approved')" value="APPROVED" />
            <el-option :label="t('common.rejected')" value="REJECTED" />
            <el-option :label="t('common.pending')" value="PENDING" />
          </el-select>
        </label>
        <label class="field field--full">
          <span>{{ t('admin.table.feedback') }}</span>
          <textarea v-model="admissionResultForm.feedback" rows="5" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.table.feedbackAt') }}</span>
          <el-date-picker
            v-model="admissionResultForm.feedbackAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :placeholder="t('admin.placeholder.selectDateTime')"
          />
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="admissionResultDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="saveAdmissionResult">
          {{ saving ? t('admin.action.saving') : t('admin.action.saveResult') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="academicResultDialogVisible" :title="academicResultForm.id ? t('admin.dialog.editAcademicResult') : t('admin.dialog.addAcademicResult')" width="720px">
      <div class="dialog-grid">
        <label class="field">
          <span>{{ t('admin.table.studentId') }}</span>
          <input v-model.number="academicResultForm.studentId" type="number" min="1" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.reportNo') }}</span>
          <input v-model="academicResultForm.reportNo" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.grade') }}</span>
          <input v-model.number="academicResultForm.grade" type="number" min="0" />
        </label>
        <label class="field">
          <span>{{ t('admin.table.enteredAt') }}</span>
          <el-date-picker
            v-model="academicResultForm.enteredAt"
            type="date"
            value-format="YYYY-MM-DD"
            :placeholder="t('admin.placeholder.selectDate')"
          />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.gradeEvaluation') }}</span>
          <input v-model="academicResultForm.gradeEvaluation" />
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.reportContent') }}</span>
          <textarea v-model="academicResultForm.reportContent" rows="6" />
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="academicResultDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="saveAcademicResult">
          {{ saving ? t('admin.action.saving') : t('admin.action.saveResult') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="consultationDialogVisible" :title="t('admin.dialog.replyConsultation')" width="680px">
      <div class="dialog-grid">
        <label class="field field--full">
          <span>{{ t('admin.field.studentQuestion') }}</span>
          <textarea v-model="consultationForm.question" rows="5" disabled />
        </label>
        <label class="field field--full">
          <span>{{ t('common.reply') }}</span>
          <textarea v-model="consultationForm.reply" rows="6" />
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="consultationDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="submitConsultationReply">
          {{ saving ? t('admin.action.saving') : t('admin.action.submitReply') }}
        </button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" :title="t('admin.dialog.reviewApplication')" width="620px">
      <div class="dialog-grid">
        <label class="field field--full">
          <span>{{ t('common.status') }}</span>
          <el-select v-model="reviewForm.status" :placeholder="t('admin.placeholder.reviewResult')">
            <el-option :label="t('common.pending')" value="PENDING" />
            <el-option :label="t('common.approved')" value="APPROVED" />
            <el-option :label="t('common.rejected')" value="REJECTED" />
          </el-select>
        </label>
        <label class="field field--full">
          <span>{{ t('admin.field.reviewComment') }}</span>
          <textarea v-model="reviewForm.reviewComment" rows="5" />
        </label>
      </div>
      <template #footer>
        <button class="ghost-button" @click="reviewDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="primary-button" :disabled="saving" @click="submitReview">
          {{ saving ? t('admin.action.saving') : t('admin.action.submitReview') }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import {
  createAcademicResult,
  createAdmissionResult,
  createMajor,
  createNews,
  createPage,
  createProvince,
  createSetting,
  createStudent,
  createUniversity,
  deleteMajor,
  deleteNews,
  deletePage,
  deleteProvince,
  deleteSetting,
  deleteStudent,
  deleteUniversity,
  fetchDashboardSummary,
  fetchAcademicResults,
  fetchAiConfig,
  fetchAdmissionResults,
  fetchConsultations,
  fetchMajorApplications,
  fetchMajors,
  fetchNews,
  fetchPages,
  fetchProvinces,
  fetchSettings,
  fetchStudents,
  fetchUniversities,
  fetchUniversityApplications,
  reviewMajorApplication,
  reviewUniversityApplication,
  replyConsultation,
  testAiConfig,
  updateAcademicResult,
  updateAiConfig,
  updateAdmissionResult,
  updateMajor,
  updateNews,
  updatePage,
  updateProvince,
  updateSetting,
  updateStudent,
  updateUniversity,
  uploadFile
} from '../api/admin'
import { useAuthStore } from '../stores/auth'
import { useLanguageStore } from '../stores/language'
import type {
  AiConfig,
  AiConfigPayload,
  AppSetting,
  AppSettingPayload,
  AcademicResult,
  AcademicResultPayload,
  AdmissionResult,
  AdmissionResultPayload,
  Consultation,
  DashboardSummary,
  Major,
  MajorApplication,
  MajorPayload,
  NewsArticle,
  NewsPayload,
  PageResult,
  Province,
  SitePage,
  SitePagePayload,
  Student,
  StudentPayload,
  University,
  UniversityApplication,
  UniversityPayload
} from '../types/admin'

type SectionId = 'dashboard' | 'students' | 'universities' | 'majors' | 'applications' | 'results' | 'consultations' | 'news' | 'pages' | 'ai' | 'settings'
type ApplicationTab = 'university' | 'major'
type ResultTab = 'admission' | 'academic'

const router = useRouter()
const auth = useAuthStore()
const language = useLanguageStore()
const t = language.t

const activeSection = ref<SectionId>('dashboard')
const applicationTab = ref<ApplicationTab>('university')
const resultTab = ref<ResultTab>('admission')
const summary = ref<DashboardSummary | null>(null)
const summaryLoading = ref(false)
const sectionLoading = ref(false)
const saving = ref(false)
const imageUploading = reactive({
  student: false,
  university: false,
  major: false,
  news: false,
  page1: false,
  page2: false,
  page3: false
})

const provinces = ref<Province[]>([])
const universityOptions = ref<University[]>([])
const pages = ref<SitePage[]>([])
const settings = ref<AppSetting[]>([])
const aiConfig = ref<AiConfig | null>(null)
const aiTesting = ref(false)
const aiTestMessage = ref('请简单介绍当前系统可以读取的招生信息。')
const aiTestResult = ref('')
const aiTestPreview = ref('')

const studentFilters = reactive({ keyword: '', page: 1, size: 8 })
const universityFilters = reactive({ keyword: '', page: 1, size: 8 })
const majorFilters = reactive({ keyword: '', page: 1, size: 8 })
const newsFilters = reactive({ keyword: '', page: 1, size: 8 })
const resultFilters = reactive({ admissionPage: 1, academicPage: 1, size: 8 })
const consultationFilters = reactive({ page: 1, size: 8 })
const applicationFilters = reactive({
  universityStatus: '',
  majorStatus: '',
  universityPage: 1,
  majorPage: 1,
  size: 8
})

const studentPage = reactive<PageResult<Student>>({ items: [], total: 0, page: 1, size: 8 })
const universityPage = reactive<PageResult<University>>({ items: [], total: 0, page: 1, size: 8 })
const majorPage = reactive<PageResult<Major>>({ items: [], total: 0, page: 1, size: 8 })
const newsPage = reactive<PageResult<NewsArticle>>({ items: [], total: 0, page: 1, size: 8 })
const universityApplicationPage = reactive<PageResult<UniversityApplication>>({ items: [], total: 0, page: 1, size: 8 })
const majorApplicationPage = reactive<PageResult<MajorApplication>>({ items: [], total: 0, page: 1, size: 8 })
const admissionResultPage = reactive<PageResult<AdmissionResult>>({ items: [], total: 0, page: 1, size: 8 })
const academicResultPage = reactive<PageResult<AcademicResult>>({ items: [], total: 0, page: 1, size: 8 })
const consultationPage = reactive<PageResult<Consultation>>({ items: [], total: 0, page: 1, size: 8 })

const provinceDialogVisible = ref(false)
const studentDialogVisible = ref(false)
const universityDialogVisible = ref(false)
const majorDialogVisible = ref(false)
const newsDialogVisible = ref(false)
const pageDialogVisible = ref(false)
const settingDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const admissionResultDialogVisible = ref(false)
const academicResultDialogVisible = ref(false)
const consultationDialogVisible = ref(false)

const provinceForm = reactive({ id: null as number | null, name: '' })
const studentForm = reactive<StudentPayload & { id: number | null }>({
  id: null,
  studentNo: '',
  password: '',
  fullName: '',
  avatarPath: '',
  gender: '',
  college: '',
  contactNumber: '',
  score: null
})
const universityForm = reactive<UniversityPayload & { id: number | null }>({
  id: null,
  name: '',
  website: '',
  imagePath: '',
  provinceId: null,
  institutionType: '',
  keyness: '',
  introduction: '',
  phone: ''
})
const majorForm = reactive<MajorPayload & { id: number | null }>({
  id: null,
  code: '',
  universityId: null,
  name: '',
  coverPath: '',
  durationOfStudy: '',
  cutOffScore: '',
  enrollmentQuota: null,
  curriculum: ''
})
const newsForm = reactive<NewsPayload & { id: number | null }>({
  id: null,
  title: '',
  introduction: '',
  picturePath: '',
  content: '',
  publishedAt: null
})
const pageForm = reactive<SitePagePayload & { id: number | null }>({
  id: null,
  slug: '',
  title: '',
  subtitle: '',
  content: '',
  picture1Path: '',
  picture2Path: '',
  picture3Path: ''
})
const settingForm = reactive<AppSettingPayload & { id: number | null }>({
  id: null,
  settingKey: '',
  settingValue: ''
})
const aiConfigForm = reactive<AiConfigPayload>({
  enabled: false,
  providerName: 'Generic HTTP API',
  endpointUrl: '',
  httpMethod: 'POST',
  apiKey: '',
  headersTemplate: '{\n  "Content-Type": "application/json",\n  "Authorization": "Bearer {{apiKey}}"\n}',
  bodyTemplate: '{\n  "model": "{{model}}",\n  "stream": true,\n  "messages": [\n    {\n      "role": "system",\n      "content": "{{systemPrompt}}\\n\\n{{context}}"\n    },\n    {\n      "role": "user",\n      "content": "{{message}}"\n    }\n  ],\n  "temperature": {{temperature}},\n  "max_tokens": {{maxTokens}}\n}',
  model: '',
  temperature: 0.7,
  maxTokens: 1024,
  systemPrompt: '你是高校招生咨询助手。只能基于当前登录学生自己的数据和公开院校、专业、资讯数据回答。',
  streamProtocol: 'AUTO',
  responseTextPath: 'choices.0.delta.content',
  doneMarker: '[DONE]',
  timeoutSeconds: 30
})
const reviewForm = reactive({
  id: null as number | null,
  type: 'university' as ApplicationTab,
  status: 'APPROVED',
  reviewComment: ''
})
const admissionResultForm = reactive<AdmissionResultPayload & { id: number | null }>({
  id: null,
  applicationId: null,
  resultStatus: 'APPROVED',
  feedback: '',
  feedbackAt: null
})
const academicResultForm = reactive<AcademicResultPayload & { id: number | null }>({
  id: null,
  studentId: null,
  reportNo: '',
  reportContent: '',
  grade: null,
  gradeEvaluation: '',
  enteredAt: null
})
const consultationForm = reactive({
  id: null as number | null,
  question: '',
  reply: ''
})

const sections = computed(() => [
  {
    id: 'dashboard' as SectionId,
    label: t('admin.section.dashboard.label'),
    title: t('admin.section.dashboard.title'),
    caption: t('admin.section.dashboard.caption'),
    description: t('admin.section.dashboard.description'),
    badge: null
  },
  {
    id: 'students' as SectionId,
    label: t('admin.section.students.label'),
    title: t('admin.section.students.title'),
    caption: t('admin.section.students.caption'),
    description: t('admin.section.students.description'),
    badge: summary.value?.studentCount ?? 0
  },
  {
    id: 'universities' as SectionId,
    label: t('admin.section.universities.label'),
    title: t('admin.section.universities.title'),
    caption: t('admin.section.universities.caption'),
    description: t('admin.section.universities.description'),
    badge: summary.value?.universityCount ?? 0
  },
  {
    id: 'majors' as SectionId,
    label: t('admin.section.majors.label'),
    title: t('admin.section.majors.title'),
    caption: t('admin.section.majors.caption'),
    description: t('admin.section.majors.description'),
    badge: summary.value?.majorCount ?? 0
  },
  {
    id: 'applications' as SectionId,
    label: t('admin.section.applications.label'),
    title: t('admin.section.applications.title'),
    caption: t('admin.section.applications.caption'),
    description: t('admin.section.applications.description'),
    badge: (summary.value?.pendingUniversityApplicationCount ?? 0) + (summary.value?.pendingMajorApplicationCount ?? 0)
  },
  {
    id: 'results' as SectionId,
    label: t('admin.section.results.label'),
    title: t('admin.section.results.title'),
    caption: t('admin.section.results.caption'),
    description: t('admin.section.results.description'),
    badge: null
  },
  {
    id: 'consultations' as SectionId,
    label: t('admin.section.consultations.label'),
    title: t('admin.section.consultations.title'),
    caption: t('admin.section.consultations.caption'),
    description: t('admin.section.consultations.description'),
    badge: null
  },
  {
    id: 'news' as SectionId,
    label: t('admin.section.news.label'),
    title: t('admin.section.news.title'),
    caption: t('admin.section.news.caption'),
    description: t('admin.section.news.description'),
    badge: summary.value?.newsCount ?? 0
  },
  {
    id: 'pages' as SectionId,
    label: t('admin.section.pages.label'),
    title: t('admin.section.pages.title'),
    caption: t('admin.section.pages.caption'),
    description: t('admin.section.pages.description'),
    badge: summary.value?.pageCount ?? 0
  },
  {
    id: 'ai' as SectionId,
    label: t('admin.section.ai.label'),
    title: t('admin.section.ai.title'),
    caption: t('admin.section.ai.caption'),
    description: t('admin.section.ai.description'),
    badge: aiConfig.value?.enabled ? 'ON' : null
  },
  {
    id: 'settings' as SectionId,
    label: t('admin.section.settings.label'),
    title: t('admin.section.settings.title'),
    caption: t('admin.section.settings.caption'),
    description: t('admin.section.settings.description'),
    badge: summary.value?.settingCount ?? 0
  }
])

const activeMeta = computed(() => sections.value.find((section) => section.id === activeSection.value) ?? sections.value[0])

const metricCards = computed(() => [
  {
    label: t('admin.metrics.universities'),
    value: summary.value?.universityCount ?? 0,
    note: t('admin.metrics.provinceNote', { count: summary.value?.provinceCount ?? 0 })
  },
  {
    label: t('admin.metrics.students'),
    value: summary.value?.studentCount ?? 0,
    note: t('admin.metrics.studentNote')
  },
  {
    label: t('admin.metrics.majors'),
    value: summary.value?.majorCount ?? 0,
    note: t('admin.metrics.majorNote')
  },
  {
    label: t('admin.metrics.pendingReviews'),
    value: (summary.value?.pendingUniversityApplicationCount ?? 0) + (summary.value?.pendingMajorApplicationCount ?? 0),
    note: t('admin.metrics.pendingNote')
  },
  {
    label: t('admin.metrics.portalContent'),
    value: (summary.value?.newsCount ?? 0) + (summary.value?.pageCount ?? 0),
    note: t('admin.metrics.contentNote')
  }
])

const provinceMap = computed(() => Object.fromEntries(provinces.value.map((province) => [province.id, province.name])))
const universityMap = computed(() => Object.fromEntries(universityOptions.value.map((item) => [item.id, item.name])))

const provinceName = (id?: number) => (id ? provinceMap.value[id] ?? `#${id}` : '-')
const universityName = (id?: number) => (id ? universityMap.value[id] ?? `#${id}` : '-')

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
const apiRootUrl = apiBaseUrl.replace(/\/api\/v1$/, '')

const imagePaths = (value?: string | null) =>
  (value ?? '')
    .split(/[,;\n]/)
    .map((path) => path.trim())
    .filter(Boolean)

const imageUrl = (value: string) => {
  const rawPath = value.trim()
  if (!rawPath) {
    return ''
  }

  if (/^https?:\/\//i.test(rawPath)) {
    try {
      const parsedUrl = new URL(rawPath)
      const uploadIndex = parsedUrl.pathname.indexOf('/upload/')
      if (uploadIndex >= 0) {
        return `${apiBaseUrl}/files/legacy/${parsedUrl.pathname.slice(uploadIndex + 1)}`
      }
    } catch {
      return rawPath
    }
    return rawPath
  }

  const cleanedPath = rawPath
    .replace(/\\/g, '/')
    .replace(/^\/+/, '')
    .replace(/^university\//, '')
    .replace(/^file\//, '')

  if (cleanedPath.startsWith('api/v1/files/')) {
    return `${apiRootUrl}/${cleanedPath}`
  }
  if (/^\d{4}\/\d{1,2}\//.test(cleanedPath)) {
    return `${apiBaseUrl}/files/${cleanedPath}`
  }
  return `${apiBaseUrl}/files/legacy/${cleanedPath}`
}

const previewImageUrls = (value?: string | null) => imagePaths(value).map(imageUrl)

const appendImagePath = (currentValue: string | undefined, newPath: string) => {
  const paths = imagePaths(currentValue)
  paths.push(newPath)
  return paths.join(',')
}

const removeImagePath = (currentValue: string | undefined, pathToRemove: string) =>
  imagePaths(currentValue)
    .filter((path) => path !== pathToRemove)
    .join(',')

const uploadImage = async (
  options: UploadRequestOptions,
  uploadingKey: keyof typeof imageUploading,
  assignPath: (relativePath: string) => void
) => {
  const file = options.file
  if (!file.type.startsWith('image/')) {
    ElMessage.warning(t('admin.message.uploadImageOnly'))
    options.onError(new Error(t('admin.message.uploadImageOnly')) as any)
    return
  }

  imageUploading[uploadingKey] = true
  try {
    const uploaded = await uploadFile(file)
    assignPath(uploaded.relativePath)
    options.onSuccess(uploaded)
    ElMessage.success(t('admin.message.imageUploaded'))
  } catch (error) {
    options.onError(error as any)
    ElMessage.error(extractError(error))
  } finally {
    imageUploading[uploadingKey] = false
  }
}

const uploadStudentAvatar = (options: UploadRequestOptions) =>
  uploadImage(options, 'student', (relativePath) => {
    studentForm.avatarPath = relativePath
  })

const uploadUniversityImage = (options: UploadRequestOptions) =>
  uploadImage(options, 'university', (relativePath) => {
    universityForm.imagePath = appendImagePath(universityForm.imagePath, relativePath)
  })

const uploadMajorCover = (options: UploadRequestOptions) =>
  uploadImage(options, 'major', (relativePath) => {
    majorForm.coverPath = appendImagePath(majorForm.coverPath, relativePath)
  })

const uploadNewsPicture = (options: UploadRequestOptions) =>
  uploadImage(options, 'news', (relativePath) => {
    newsForm.picturePath = relativePath
  })

const uploadPagePicture1 = (options: UploadRequestOptions) =>
  uploadImage(options, 'page1', (relativePath) => {
    pageForm.picture1Path = relativePath
  })

const uploadPagePicture2 = (options: UploadRequestOptions) =>
  uploadImage(options, 'page2', (relativePath) => {
    pageForm.picture2Path = relativePath
  })

const uploadPagePicture3 = (options: UploadRequestOptions) =>
  uploadImage(options, 'page3', (relativePath) => {
    pageForm.picture3Path = relativePath
  })

const removeUniversityImage = (path: string) => {
  universityForm.imagePath = removeImagePath(universityForm.imagePath, path)
}

const removeMajorCover = (path: string) => {
  majorForm.coverPath = removeImagePath(majorForm.coverPath, path)
}

const removeNewsPicture = (path: string) => {
  newsForm.picturePath = removeImagePath(newsForm.picturePath, path)
}

const pageImagePaths = (page: SitePage) => [
  ...imagePaths(page.picture1Path),
  ...imagePaths(page.picture2Path),
  ...imagePaths(page.picture3Path)
]

const pagePreviewImageUrls = (page: SitePage) => pageImagePaths(page).map(imageUrl)

const extractError = (error: any) =>
  error?.response?.data?.detail ??
  error?.response?.data?.message ??
  error?.message ??
  t('common.requestFailed')

const statusTagType = (status: string) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

const statusLabel = (status: string) => {
  if (status === 'APPROVED') return t('common.approved')
  if (status === 'REJECTED') return t('common.rejected')
  if (status === 'PENDING') return t('common.pending')
  return status || '-'
}

const assignPage = <T>(target: PageResult<T>, source: PageResult<T>) => {
  target.items = source.items
  target.total = source.total
  target.page = source.page
  target.size = source.size
}

const resetProvinceForm = () => {
  provinceForm.id = null
  provinceForm.name = ''
}

const resetStudentForm = () => {
  studentForm.id = null
  studentForm.studentNo = ''
  studentForm.password = ''
  studentForm.fullName = ''
  studentForm.avatarPath = ''
  studentForm.gender = ''
  studentForm.college = ''
  studentForm.contactNumber = ''
  studentForm.score = null
}

const resetUniversityForm = () => {
  universityForm.id = null
  universityForm.name = ''
  universityForm.website = ''
  universityForm.imagePath = ''
  universityForm.provinceId = null
  universityForm.institutionType = ''
  universityForm.keyness = ''
  universityForm.introduction = ''
  universityForm.phone = ''
}

const resetMajorForm = () => {
  majorForm.id = null
  majorForm.code = ''
  majorForm.universityId = null
  majorForm.name = ''
  majorForm.coverPath = ''
  majorForm.durationOfStudy = ''
  majorForm.cutOffScore = ''
  majorForm.enrollmentQuota = null
  majorForm.curriculum = ''
}

const resetNewsForm = () => {
  newsForm.id = null
  newsForm.title = ''
  newsForm.introduction = ''
  newsForm.picturePath = ''
  newsForm.content = ''
  newsForm.publishedAt = null
}

const resetPageForm = () => {
  pageForm.id = null
  pageForm.slug = ''
  pageForm.title = ''
  pageForm.subtitle = ''
  pageForm.content = ''
  pageForm.picture1Path = ''
  pageForm.picture2Path = ''
  pageForm.picture3Path = ''
}

const resetSettingForm = () => {
  settingForm.id = null
  settingForm.settingKey = ''
  settingForm.settingValue = ''
}

const applyAiConfig = (config: AiConfig) => {
  aiConfig.value = config
  aiConfigForm.enabled = config.enabled
  aiConfigForm.providerName = config.providerName
  aiConfigForm.endpointUrl = config.endpointUrl
  aiConfigForm.httpMethod = 'POST'
  aiConfigForm.apiKey = ''
  aiConfigForm.headersTemplate = config.headersTemplate
  aiConfigForm.bodyTemplate = config.bodyTemplate
  aiConfigForm.model = config.model
  aiConfigForm.temperature = config.temperature
  aiConfigForm.maxTokens = config.maxTokens
  aiConfigForm.systemPrompt = config.systemPrompt
  aiConfigForm.streamProtocol = config.streamProtocol
  aiConfigForm.responseTextPath = config.responseTextPath
  aiConfigForm.doneMarker = config.doneMarker
  aiConfigForm.timeoutSeconds = config.timeoutSeconds
}

const resetAdmissionResultForm = () => {
  admissionResultForm.id = null
  admissionResultForm.applicationId = null
  admissionResultForm.resultStatus = 'APPROVED'
  admissionResultForm.feedback = ''
  admissionResultForm.feedbackAt = null
}

const resetAcademicResultForm = () => {
  academicResultForm.id = null
  academicResultForm.studentId = null
  academicResultForm.reportNo = ''
  academicResultForm.reportContent = ''
  academicResultForm.grade = null
  academicResultForm.gradeEvaluation = ''
  academicResultForm.enteredAt = null
}

const resetConsultationForm = () => {
  consultationForm.id = null
  consultationForm.question = ''
  consultationForm.reply = ''
}

const ensureReferenceData = async () => {
  const [provinceList, universityResult] = await Promise.all([
    fetchProvinces(),
    fetchUniversities({ page: 1, size: 200 })
  ])
  provinces.value = provinceList
  universityOptions.value = universityResult.items
}

const loadSummary = async () => {
  summaryLoading.value = true
  try {
    summary.value = await fetchDashboardSummary()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    summaryLoading.value = false
  }
}

const loadStudents = async () => {
  sectionLoading.value = true
  try {
    assignPage(studentPage, await fetchStudents(studentFilters))
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadUniversities = async () => {
  sectionLoading.value = true
  try {
    assignPage(universityPage, await fetchUniversities(universityFilters))
    await ensureReferenceData()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadMajors = async () => {
  sectionLoading.value = true
  try {
    assignPage(majorPage, await fetchMajors(majorFilters))
    await ensureReferenceData()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadApplications = async () => {
  sectionLoading.value = true
  try {
    if (applicationTab.value === 'university') {
      assignPage(
        universityApplicationPage,
        await fetchUniversityApplications({
          page: applicationFilters.universityPage,
          size: applicationFilters.size,
          status: applicationFilters.universityStatus || undefined
        })
      )
    } else {
      assignPage(
        majorApplicationPage,
        await fetchMajorApplications({
          page: applicationFilters.majorPage,
          size: applicationFilters.size,
          status: applicationFilters.majorStatus || undefined
        })
      )
    }
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadResults = async () => {
  sectionLoading.value = true
  try {
    if (resultTab.value === 'admission') {
      assignPage(admissionResultPage, await fetchAdmissionResults({
        page: resultFilters.admissionPage,
        size: resultFilters.size
      }))
    } else {
      assignPage(academicResultPage, await fetchAcademicResults({
        page: resultFilters.academicPage,
        size: resultFilters.size
      }))
    }
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadConsultations = async () => {
  sectionLoading.value = true
  try {
    assignPage(consultationPage, await fetchConsultations(consultationFilters))
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadNews = async () => {
  sectionLoading.value = true
  try {
    assignPage(newsPage, await fetchNews(newsFilters))
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadPages = async () => {
  sectionLoading.value = true
  try {
    pages.value = await fetchPages()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadSettings = async () => {
  sectionLoading.value = true
  try {
    settings.value = await fetchSettings()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadAiConfig = async () => {
  sectionLoading.value = true
  try {
    applyAiConfig(await fetchAiConfig())
    aiTestResult.value = ''
    aiTestPreview.value = ''
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    sectionLoading.value = false
  }
}

const loadCurrentSection = async () => {
  switch (activeSection.value) {
    case 'students':
      await loadStudents()
      break
    case 'universities':
      await loadUniversities()
      break
    case 'majors':
      await loadMajors()
      break
    case 'applications':
      await loadApplications()
      break
    case 'results':
      await loadResults()
      break
    case 'consultations':
      await loadConsultations()
      break
    case 'news':
      await loadNews()
      break
    case 'pages':
      await loadPages()
      break
    case 'ai':
      await loadAiConfig()
      break
    case 'settings':
      await loadSettings()
      break
    default:
      await Promise.all([loadSummary(), ensureReferenceData()])
  }
}

const activateSection = async (section: SectionId) => {
  activeSection.value = section
  await loadCurrentSection()
}

const refreshAll = async () => {
  await loadSummary()
  await loadCurrentSection()
}

const onUniversityPageChange = async (page: number) => {
  universityFilters.page = page
  await loadUniversities()
}

const onStudentPageChange = async (page: number) => {
  studentFilters.page = page
  await loadStudents()
}

const onMajorPageChange = async (page: number) => {
  majorFilters.page = page
  await loadMajors()
}

const onNewsPageChange = async (page: number) => {
  newsFilters.page = page
  await loadNews()
}

const onApplicationPageChange = async (page: number) => {
  if (applicationTab.value === 'university') {
    applicationFilters.universityPage = page
  } else {
    applicationFilters.majorPage = page
  }
  await loadApplications()
}

const onResultPageChange = async (page: number) => {
  if (resultTab.value === 'admission') {
    resultFilters.admissionPage = page
  } else {
    resultFilters.academicPage = page
  }
  await loadResults()
}

const onConsultationPageChange = async (page: number) => {
  consultationFilters.page = page
  await loadConsultations()
}

const switchApplicationTab = async (tab: ApplicationTab) => {
  applicationTab.value = tab
  await loadApplications()
}

const switchResultTab = async (tab: ResultTab) => {
  resultTab.value = tab
  await loadResults()
}

const reloadApplications = async () => {
  if (applicationTab.value === 'university') {
    applicationFilters.universityPage = 1
  } else {
    applicationFilters.majorPage = 1
  }
  await loadApplications()
}

const editProvince = (province: Province) => {
  provinceForm.id = province.id
  provinceForm.name = province.name
}

const saveProvince = async () => {
  if (!provinceForm.name.trim()) {
    ElMessage.warning(t('admin.message.provinceRequired'))
    return
  }
  saving.value = true
  try {
    if (provinceForm.id) {
      await updateProvince(provinceForm.id, { name: provinceForm.name.trim() })
      ElMessage.success(t('admin.message.provinceUpdated'))
    } else {
      await createProvince({ name: provinceForm.name.trim() })
      ElMessage.success(t('admin.message.provinceCreated'))
    }
    resetProvinceForm()
    await ensureReferenceData()
    await loadSummary()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const removeProvince = async (id: number) => {
  await ElMessageBox.confirm(t('admin.message.deleteProvince'), t('common.confirm'), { type: 'warning' })
  try {
    await deleteProvince(id)
    ElMessage.success(t('admin.message.provinceDeleted'))
    await ensureReferenceData()
    await loadSummary()
  } catch (error) {
    ElMessage.error(extractError(error))
  }
}

const openStudentDialog = (record?: Student) => {
  resetStudentForm()
  if (record) {
    studentForm.id = record.id
    studentForm.studentNo = record.studentNo
    studentForm.fullName = record.fullName
    studentForm.avatarPath = record.avatarPath ?? ''
    studentForm.gender = record.gender ?? ''
    studentForm.college = record.college ?? ''
    studentForm.contactNumber = record.contactNumber ?? ''
    studentForm.score = record.score ?? null
  }
  studentDialogVisible.value = true
}

const saveStudent = async () => {
  if (!studentForm.studentNo.trim() || !studentForm.fullName.trim()) {
    ElMessage.warning(t('admin.message.studentRequired'))
    return
  }
  if (!studentForm.id && !studentForm.password?.trim()) {
    ElMessage.warning(t('admin.message.studentRequired'))
    return
  }
  saving.value = true
  try {
    const payload: StudentPayload = {
      studentNo: studentForm.studentNo.trim(),
      password: studentForm.password?.trim() || undefined,
      fullName: studentForm.fullName.trim(),
      avatarPath: studentForm.avatarPath?.trim(),
      gender: studentForm.gender?.trim(),
      college: studentForm.college?.trim(),
      contactNumber: studentForm.contactNumber?.trim(),
      score: studentForm.score === undefined || studentForm.score === null || Number.isNaN(studentForm.score)
        ? null
        : Number(studentForm.score)
    }
    if (studentForm.id) {
      await updateStudent(studentForm.id, payload)
      ElMessage.success(t('admin.message.studentUpdated'))
    } else {
      await createStudent(payload)
      ElMessage.success(t('admin.message.studentCreated'))
    }
    studentDialogVisible.value = false
    await Promise.all([loadStudents(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const removeStudent = async (id: number) => {
  await ElMessageBox.confirm(t('admin.message.deleteStudent'), t('common.confirm'), { type: 'warning' })
  try {
    await deleteStudent(id)
    ElMessage.success(t('admin.message.studentDeleted'))
    await Promise.all([loadStudents(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  }
}

const openUniversityDialog = (record?: University) => {
  resetUniversityForm()
  if (record) {
    universityForm.id = record.id
    universityForm.name = record.name
    universityForm.website = record.website ?? ''
    universityForm.imagePath = record.imagePath ?? ''
    universityForm.provinceId = record.provinceId ?? null
    universityForm.institutionType = record.institutionType ?? ''
    universityForm.keyness = record.keyness ?? ''
    universityForm.introduction = record.introduction ?? ''
    universityForm.phone = record.phone ?? ''
  }
  universityDialogVisible.value = true
}

const saveUniversity = async () => {
  if (!universityForm.name.trim() || !universityForm.provinceId) {
    ElMessage.warning(t('admin.message.universityRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      name: universityForm.name.trim(),
      website: universityForm.website?.trim(),
      imagePath: universityForm.imagePath?.trim(),
      provinceId: universityForm.provinceId,
      institutionType: universityForm.institutionType?.trim(),
      keyness: universityForm.keyness?.trim(),
      introduction: universityForm.introduction?.trim(),
      phone: universityForm.phone?.trim()
    }
    if (universityForm.id) {
      await updateUniversity(universityForm.id, payload)
      ElMessage.success(t('admin.message.universityUpdated'))
    } else {
      await createUniversity(payload)
      ElMessage.success(t('admin.message.universityCreated'))
    }
    universityDialogVisible.value = false
    await Promise.all([loadUniversities(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const removeUniversity = async (id: number) => {
  await ElMessageBox.confirm(t('admin.message.deleteUniversity'), t('common.confirm'), { type: 'warning' })
  try {
    await deleteUniversity(id)
    ElMessage.success(t('admin.message.universityDeleted'))
    await Promise.all([loadUniversities(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  }
}

const openMajorDialog = (record?: Major) => {
  resetMajorForm()
  if (record) {
    majorForm.id = record.id
    majorForm.code = record.code
    majorForm.universityId = record.universityId
    majorForm.name = record.name
    majorForm.coverPath = record.coverPath ?? ''
    majorForm.durationOfStudy = record.durationOfStudy ?? ''
    majorForm.cutOffScore = record.cutOffScore ?? ''
    majorForm.enrollmentQuota = record.enrollmentQuota ?? null
    majorForm.curriculum = record.curriculum ?? ''
  }
  majorDialogVisible.value = true
}

const saveMajor = async () => {
  if (!majorForm.code.trim() || !majorForm.name.trim() || !majorForm.universityId) {
    ElMessage.warning(t('admin.message.majorRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      code: majorForm.code.trim(),
      universityId: majorForm.universityId,
      name: majorForm.name.trim(),
      coverPath: majorForm.coverPath?.trim(),
      durationOfStudy: majorForm.durationOfStudy?.trim(),
      cutOffScore: majorForm.cutOffScore?.trim(),
      enrollmentQuota: majorForm.enrollmentQuota,
      curriculum: majorForm.curriculum?.trim()
    }
    if (majorForm.id) {
      await updateMajor(majorForm.id, payload)
      ElMessage.success(t('admin.message.majorUpdated'))
    } else {
      await createMajor(payload)
      ElMessage.success(t('admin.message.majorCreated'))
    }
    majorDialogVisible.value = false
    await Promise.all([loadMajors(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const removeMajor = async (id: number) => {
  await ElMessageBox.confirm(t('admin.message.deleteMajor'), t('common.confirm'), { type: 'warning' })
  try {
    await deleteMajor(id)
    ElMessage.success(t('admin.message.majorDeleted'))
    await Promise.all([loadMajors(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  }
}

const openNewsDialog = (record?: NewsArticle) => {
  resetNewsForm()
  if (record) {
    newsForm.id = record.id
    newsForm.title = record.title
    newsForm.introduction = record.introduction ?? ''
    newsForm.picturePath = record.picturePath ?? ''
    newsForm.content = record.content
    newsForm.publishedAt = record.publishedAt ?? null
  }
  newsDialogVisible.value = true
}

const saveNews = async () => {
  if (!newsForm.title.trim() || !newsForm.content.trim()) {
    ElMessage.warning(t('admin.message.newsRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      title: newsForm.title.trim(),
      introduction: newsForm.introduction?.trim(),
      picturePath: newsForm.picturePath?.trim(),
      content: newsForm.content.trim(),
      publishedAt: newsForm.publishedAt || null
    }
    if (newsForm.id) {
      await updateNews(newsForm.id, payload)
      ElMessage.success(t('admin.message.newsUpdated'))
    } else {
      await createNews(payload)
      ElMessage.success(t('admin.message.newsCreated'))
    }
    newsDialogVisible.value = false
    await Promise.all([loadNews(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const removeNews = async (id: number) => {
  await ElMessageBox.confirm(t('admin.message.deleteNews'), t('common.confirm'), { type: 'warning' })
  try {
    await deleteNews(id)
    ElMessage.success(t('admin.message.newsDeleted'))
    await Promise.all([loadNews(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  }
}

const openPageDialog = (record?: SitePage) => {
  resetPageForm()
  if (record) {
    pageForm.id = record.id
    pageForm.slug = record.slug
    pageForm.title = record.title
    pageForm.subtitle = record.subtitle ?? ''
    pageForm.content = record.content
    pageForm.picture1Path = record.picture1Path ?? ''
    pageForm.picture2Path = record.picture2Path ?? ''
    pageForm.picture3Path = record.picture3Path ?? ''
  }
  pageDialogVisible.value = true
}

const savePage = async () => {
  if (!pageForm.slug.trim() || !pageForm.title.trim() || !pageForm.content.trim()) {
    ElMessage.warning(t('admin.message.pageRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      slug: pageForm.slug.trim(),
      title: pageForm.title.trim(),
      subtitle: pageForm.subtitle?.trim(),
      content: pageForm.content.trim(),
      picture1Path: pageForm.picture1Path?.trim(),
      picture2Path: pageForm.picture2Path?.trim(),
      picture3Path: pageForm.picture3Path?.trim()
    }
    if (pageForm.id) {
      await updatePage(pageForm.id, payload)
      ElMessage.success(t('admin.message.pageUpdated'))
    } else {
      await createPage(payload)
      ElMessage.success(t('admin.message.pageCreated'))
    }
    pageDialogVisible.value = false
    await Promise.all([loadPages(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const removePage = async (id: number) => {
  await ElMessageBox.confirm(t('admin.message.deletePage'), t('common.confirm'), { type: 'warning' })
  try {
    await deletePage(id)
    ElMessage.success(t('admin.message.pageDeleted'))
    await Promise.all([loadPages(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  }
}

const openSettingDialog = (record?: AppSetting) => {
  resetSettingForm()
  if (record) {
    settingForm.id = record.id
    settingForm.settingKey = record.settingKey
    settingForm.settingValue = record.settingValue ?? ''
  }
  settingDialogVisible.value = true
}

const saveSetting = async () => {
  if (!settingForm.settingKey.trim()) {
    ElMessage.warning(t('admin.message.settingRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      settingKey: settingForm.settingKey.trim(),
      settingValue: settingForm.settingValue?.trim()
    }
    if (settingForm.id) {
      await updateSetting(settingForm.id, payload)
      ElMessage.success(t('admin.message.settingUpdated'))
    } else {
      await createSetting(payload)
      ElMessage.success(t('admin.message.settingCreated'))
    }
    settingDialogVisible.value = false
    await Promise.all([loadSettings(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const removeSetting = async (id: number) => {
  await ElMessageBox.confirm(t('admin.message.deleteSetting'), t('common.confirm'), { type: 'warning' })
  try {
    await deleteSetting(id)
    ElMessage.success(t('admin.message.settingDeleted'))
    await Promise.all([loadSettings(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  }
}

const parseHeadersTemplate = () => {
  try {
    JSON.parse(aiConfigForm.headersTemplate.replace(/\{\{apiKey\}\}/g, ''))
    return true
  } catch {
    ElMessage.warning(t('admin.ai.templatesInvalid'))
    return false
  }
}

const aiConfigPayload = (): AiConfigPayload => ({
  enabled: aiConfigForm.enabled,
  providerName: aiConfigForm.providerName.trim(),
  endpointUrl: aiConfigForm.endpointUrl.trim(),
  httpMethod: 'POST',
  apiKey: aiConfigForm.apiKey?.trim() || undefined,
  headersTemplate: aiConfigForm.headersTemplate.trim(),
  bodyTemplate: aiConfigForm.bodyTemplate.trim(),
  model: aiConfigForm.model.trim(),
  temperature: Number(aiConfigForm.temperature ?? 0.7),
  maxTokens: Number(aiConfigForm.maxTokens ?? 1024),
  systemPrompt: aiConfigForm.systemPrompt.trim(),
  streamProtocol: aiConfigForm.streamProtocol,
  responseTextPath: aiConfigForm.responseTextPath.trim(),
  doneMarker: aiConfigForm.doneMarker.trim() || '[DONE]',
  timeoutSeconds: Number(aiConfigForm.timeoutSeconds ?? 30)
})

const saveAiConfig = async (showMessage = true) => {
  if (aiConfigForm.enabled && !aiConfigForm.endpointUrl.trim()) {
    ElMessage.warning(t('admin.ai.endpointRequired'))
    return false
  }
  if (!parseHeadersTemplate()) {
    return false
  }
  saving.value = true
  try {
    applyAiConfig(await updateAiConfig(aiConfigPayload()))
    if (showMessage) {
      ElMessage.success(t('admin.message.aiConfigSaved'))
    }
    return true
  } catch (error) {
    ElMessage.error(extractError(error))
    return false
  } finally {
    saving.value = false
  }
}

const runAiConfigTest = async () => {
  aiTestResult.value = ''
  aiTestPreview.value = ''
  if (!(await saveAiConfig(false))) {
    return
  }
  aiTesting.value = true
  try {
    const result = await testAiConfig(aiTestMessage.value.trim() || 'Hello')
    aiTestResult.value = result.message
    aiTestPreview.value = result.preview ?? ''
    if (result.success) {
      ElMessage.success(result.message)
    } else {
      ElMessage.warning(result.message)
    }
  } catch (error) {
    aiTestResult.value = extractError(error)
    ElMessage.error(aiTestResult.value)
  } finally {
    aiTesting.value = false
  }
}

const openAdmissionResultDialog = (record?: AdmissionResult) => {
  resetAdmissionResultForm()
  if (record) {
    admissionResultForm.id = record.id
    admissionResultForm.applicationId = record.applicationId ?? null
    admissionResultForm.resultStatus = record.resultStatus
    admissionResultForm.feedback = record.feedback ?? ''
    admissionResultForm.feedbackAt = record.feedbackAt ?? null
  }
  admissionResultDialogVisible.value = true
}

const saveAdmissionResult = async () => {
  if (!admissionResultForm.applicationId || !admissionResultForm.resultStatus.trim()) {
    ElMessage.warning(t('admin.message.admissionResultRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      applicationId: admissionResultForm.applicationId,
      resultStatus: admissionResultForm.resultStatus,
      feedback: admissionResultForm.feedback?.trim(),
      feedbackAt: admissionResultForm.feedbackAt || null
    }
    if (admissionResultForm.id) {
      await updateAdmissionResult(admissionResultForm.id, payload)
      ElMessage.success(t('admin.message.admissionResultUpdated'))
    } else {
      await createAdmissionResult(payload)
      ElMessage.success(t('admin.message.admissionResultCreated'))
    }
    admissionResultDialogVisible.value = false
    await loadResults()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const openAcademicResultDialog = (record?: AcademicResult) => {
  resetAcademicResultForm()
  if (record) {
    academicResultForm.id = record.id
    academicResultForm.studentId = record.studentId ?? null
    academicResultForm.reportNo = record.reportNo ?? ''
    academicResultForm.reportContent = record.reportContent ?? ''
    academicResultForm.grade = record.grade ?? null
    academicResultForm.gradeEvaluation = record.gradeEvaluation ?? ''
    academicResultForm.enteredAt = record.enteredAt ?? null
  }
  academicResultDialogVisible.value = true
}

const saveAcademicResult = async () => {
  if (!academicResultForm.studentId) {
    ElMessage.warning(t('admin.message.academicResultRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      studentId: academicResultForm.studentId,
      reportNo: academicResultForm.reportNo?.trim(),
      reportContent: academicResultForm.reportContent?.trim(),
      grade: academicResultForm.grade,
      gradeEvaluation: academicResultForm.gradeEvaluation?.trim(),
      enteredAt: academicResultForm.enteredAt || null
    }
    if (academicResultForm.id) {
      await updateAcademicResult(academicResultForm.id, payload)
      ElMessage.success(t('admin.message.academicResultUpdated'))
    } else {
      await createAcademicResult(payload)
      ElMessage.success(t('admin.message.academicResultCreated'))
    }
    academicResultDialogVisible.value = false
    await loadResults()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const openConsultationDialog = (record: Consultation) => {
  resetConsultationForm()
  consultationForm.id = record.id
  consultationForm.question = record.question
  consultationForm.reply = record.reply ?? ''
  consultationDialogVisible.value = true
}

const submitConsultationReply = async () => {
  if (!consultationForm.id || !consultationForm.reply.trim()) {
    ElMessage.warning(t('admin.message.replyRequired'))
    return
  }
  saving.value = true
  try {
    await replyConsultation(consultationForm.id, { reply: consultationForm.reply.trim() })
    consultationDialogVisible.value = false
    ElMessage.success(t('admin.message.replySubmitted'))
    await loadConsultations()
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const openReviewDialog = (type: ApplicationTab, record: UniversityApplication | MajorApplication) => {
  reviewForm.id = record.id
  reviewForm.type = type
  reviewForm.status = record.status
  reviewForm.reviewComment = record.reviewComment ?? ''
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.id || !reviewForm.status) {
    ElMessage.warning(t('admin.message.reviewRequired'))
    return
  }
  saving.value = true
  try {
    const payload = {
      status: reviewForm.status,
      reviewComment: reviewForm.reviewComment.trim()
    }
    if (reviewForm.type === 'university') {
      await reviewUniversityApplication(reviewForm.id, payload)
    } else {
      await reviewMajorApplication(reviewForm.id, payload)
    }
    reviewDialogVisible.value = false
    ElMessage.success(t('admin.message.reviewSubmitted'))
    await Promise.all([loadApplications(), loadSummary()])
  } catch (error) {
    ElMessage.error(extractError(error))
  } finally {
    saving.value = false
  }
}

const logout = async () => {
  await auth.logout()
  await router.replace('/login')
}

onMounted(async () => {
  await Promise.all([loadSummary(), ensureReferenceData()])
})
</script>

<style scoped>
.workspace {
  --ink-900: #122033;
  --ink-700: #415066;
  --ink-500: #687487;
  --ink-300: #c9d0db;
  --mist: #eef2f7;
  --paper: #fffdf8;
  --sand: #f3ebdd;
  --gold: #d39c2f;
  --gold-soft: #f7dca0;
  --navy: #14263f;
  --navy-soft: #1e3557;
  --line: rgba(18, 32, 51, 0.08);
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(260px, 310px) minmax(0, 1fr);
  gap: 24px;
  padding: 24px;
  background:
    radial-gradient(circle at 10% 12%, rgba(211, 156, 47, 0.12), transparent 24%),
    radial-gradient(circle at 82% 12%, rgba(71, 114, 196, 0.12), transparent 22%),
    linear-gradient(180deg, #f7f2e8 0%, #eef3f8 100%);
}

.workspace__sidebar,
.data-card,
.panel-soft {
  border: 1px solid var(--line);
  box-shadow: 0 24px 48px rgba(19, 31, 48, 0.08);
}

.workspace__sidebar {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 22px;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(20, 38, 63, 0.98), rgba(16, 32, 53, 0.98)),
    var(--navy);
  color: #f7f1e7;
}

.brand-block h1,
.workspace__header h2,
.metric-card strong,
.quick-actions h3,
.insight-panel h3 {
  font-family: Georgia, "Times New Roman", serif;
}

.eyebrow {
  margin: 0 0 10px;
  color: var(--gold-soft);
  font: 700 0.76rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.brand-block h1 {
  margin: 0;
  font-size: 2rem;
  line-height: 1.06;
}

.brand-block p:last-child {
  margin: 14px 0 0;
  color: rgba(247, 241, 231, 0.72);
  font: 500 0.96rem/1.7 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.nav-button {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.03);
  color: inherit;
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.nav-button > div {
  min-width: 0;
}

.nav-button:hover,
.nav-button.is-active {
  transform: translateY(-1px);
  background: linear-gradient(135deg, rgba(211, 156, 47, 0.18), rgba(255, 255, 255, 0.05));
  border-color: rgba(247, 220, 160, 0.4);
}

.nav-button strong {
  display: block;
  margin-bottom: 4px;
  font: 700 0.96rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.nav-button span {
  display: block;
  color: rgba(247, 241, 231, 0.66);
  font: 500 0.82rem/1.5 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.nav-button em {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff9ec;
  font: 700 0.86rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  font-style: normal;
}

.profile-card {
  margin-top: auto;
  padding: 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.06);
}

.profile-card strong {
  display: block;
  margin-bottom: 6px;
  font: 700 1rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.profile-card span {
  display: block;
  margin-bottom: 18px;
  color: rgba(247, 241, 231, 0.68);
  font: 500 0.9rem/1.4 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.workspace__main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.workspace__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 8px 4px 0;
}

.workspace__header h2 {
  margin: 0;
  color: var(--ink-900);
  font-size: clamp(2rem, 3vw, 3rem);
  line-height: 1.05;
}

.workspace__header p:last-child {
  max-width: 58ch;
  margin: 12px 0 0;
  color: var(--ink-500);
  font: 500 0.98rem/1.75 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.section-stack {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  padding: 20px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.85), rgba(255, 253, 248, 0.95));
}

.metric-card p {
  margin: 0 0 14px;
  color: var(--ink-500);
  font: 700 0.82rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.metric-card strong {
  display: block;
  color: var(--ink-900);
  font-size: 2.4rem;
  line-height: 1;
}

.metric-card span {
  display: block;
  margin-top: 12px;
  color: var(--ink-700);
  font: 500 0.95rem/1.55 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.split-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 18px;
}

.panel-soft,
.data-card {
  min-width: 0;
  border-radius: 26px;
  background: rgba(255, 253, 248, 0.95);
}

.insight-panel,
.quick-actions {
  padding: 24px;
}

.insight-panel h3,
.quick-actions h3 {
  margin: 0;
  color: var(--ink-900);
  font-size: 1.7rem;
}

.insight-list {
  margin: 18px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 14px;
}

.insight-list li {
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--mist);
  color: var(--ink-700);
  font: 500 0.96rem/1.65 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.insight-list strong {
  color: var(--ink-900);
}

.quick-actions__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.action-tile {
  padding: 18px;
  border: 1px solid rgba(20, 38, 63, 0.08);
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(246, 239, 226, 0.95));
  color: var(--ink-900);
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.action-tile:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 24px rgba(20, 38, 63, 0.08);
}

.action-tile strong,
.action-tile span {
  display: block;
}

.action-tile strong {
  margin-bottom: 8px;
  font: 700 1rem/1.3 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.action-tile span {
  color: var(--ink-500);
  font: 500 0.9rem/1.55 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 24px;
}

.toolbar--compact {
  padding: 0;
  border: 0;
  box-shadow: none;
  background: transparent;
}

.toolbar__search {
  min-width: 220px;
  flex: 1;
}

.toolbar__search input,
.dialog-toolbar input,
.field input,
.field textarea {
  width: 100%;
  border: 1px solid rgba(18, 32, 51, 0.12);
  border-radius: 16px;
  outline: none;
  background: #fff;
  color: var(--ink-900);
  box-sizing: border-box;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.toolbar__search input,
.dialog-toolbar input,
.field input {
  min-height: 48px;
  padding: 0 16px;
}

.field textarea {
  min-height: 120px;
  padding: 14px 16px;
  resize: vertical;
}

.toolbar__search input:focus,
.dialog-toolbar input:focus,
.field input:focus,
.field textarea:focus {
  border-color: rgba(211, 156, 47, 0.5);
  box-shadow: 0 0 0 4px rgba(211, 156, 47, 0.12);
}

.toolbar__search--static span {
  color: var(--ink-500);
  font: 500 0.96rem/1.6 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.toolbar__search--select :deep(.el-select) {
  width: 100%;
}

.toolbar__actions,
.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.primary-button,
.ghost-button {
  border: 0;
  border-radius: 16px;
  cursor: pointer;
  font: 800 0.88rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.primary-button {
  padding: 15px 18px;
  background: linear-gradient(135deg, var(--gold), #e7b85c);
  color: #132136;
  box-shadow: 0 16px 24px rgba(211, 156, 47, 0.2);
}

.ghost-button {
  padding: 14px 16px;
  background: rgba(20, 38, 63, 0.08);
  color: var(--ink-900);
}

.primary-button:hover,
.ghost-button:hover {
  transform: translateY(-1px);
}

.primary-button:disabled,
.ghost-button:disabled {
  opacity: 0.6;
  cursor: wait;
}

.data-card {
  overflow: hidden;
  padding: 18px;
}

.ai-config-card {
  display: grid;
  gap: 18px;
}

.ai-switch-field {
  min-height: 48px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
}

.ai-secret-note {
  margin: 0;
  color: var(--ink-500);
  font: 600 0.9rem/1.5 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.ai-config-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.ai-test-result {
  border: 1px solid rgba(18, 32, 51, 0.1);
  border-radius: 18px;
  padding: 14px 16px;
  background: rgba(18, 32, 51, 0.04);
}

.ai-test-result strong {
  display: block;
  margin-bottom: 8px;
  color: var(--ink-800);
}

.ai-test-result p {
  margin: 0;
  color: var(--ink-700);
  line-height: 1.6;
}

.ai-test-result pre {
  margin: 12px 0 0;
  overflow: auto;
  white-space: pre-wrap;
  color: var(--ink-700);
  font: 600 0.86rem/1.6 Consolas, "Courier New", monospace;
}

.tabs-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tab-switcher {
  display: inline-flex;
  gap: 10px;
}

.tab-button {
  padding: 12px 16px;
  border: 1px solid rgba(18, 32, 51, 0.1);
  border-radius: 999px;
  background: #fff;
  color: var(--ink-700);
  cursor: pointer;
  font: 700 0.9rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.tab-button.is-active {
  background: linear-gradient(135deg, var(--navy), var(--navy-soft));
  color: #fff7e9;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.dialog-toolbar {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  margin-bottom: 16px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.field {
  min-width: 0;
  display: block;
}

.field--full {
  grid-column: 1 / -1;
}

.field span {
  display: block;
  margin-bottom: 8px;
  color: var(--ink-700);
  font: 700 0.8rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.table-images,
.form-images {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.image-uploader {
  display: inline-flex;
}

.upload-button,
.image-remove {
  border: 0;
  cursor: pointer;
  font: 800 0.78rem/1 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  transition: transform 0.2s ease, opacity 0.2s ease, background 0.2s ease;
}

.upload-button {
  min-height: 42px;
  padding: 0 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--navy), var(--navy-soft));
  color: #fff7e9;
}

.upload-button:hover,
.image-remove:hover {
  transform: translateY(-1px);
}

.upload-button:disabled {
  cursor: wait;
  opacity: 0.62;
}

.table-thumb,
.form-thumb {
  overflow: hidden;
  border: 1px solid rgba(18, 32, 51, 0.1);
  background: #f5efe3;
  box-shadow: 0 10px 18px rgba(20, 38, 63, 0.08);
}

.table-thumb {
  width: 46px;
  height: 46px;
  border-radius: 12px;
}

.form-images {
  margin-top: 10px;
}

.form-image-item {
  position: relative;
  display: grid;
  gap: 6px;
  justify-items: center;
}

.form-thumb {
  width: 84px;
  height: 84px;
  border-radius: 16px;
}

.image-remove {
  padding: 7px 10px;
  border-radius: 999px;
  background: rgba(176, 47, 47, 0.1);
  color: #9d2c2c;
}

.image-count,
.muted-text {
  color: var(--ink-500);
  font: 700 0.82rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
}

.image-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 999px;
  background: rgba(20, 38, 63, 0.08);
}

.workspace :deep(.el-table) {
  --el-table-header-bg-color: #f5efe3;
  --el-table-header-text-color: #2b3a4d;
  --el-table-row-hover-bg-color: #fbf6eb;
  width: 100%;
  border-radius: 18px;
}

.workspace :deep(.el-table__inner-wrapper),
.workspace :deep(.el-table__body-wrapper),
.workspace :deep(.el-scrollbar__wrap) {
  min-width: 0;
}

.workspace :deep(.el-table .cell) {
  word-break: break-word;
}

.workspace :deep(.el-table th.el-table__cell) {
  font: 800 0.78rem/1.2 "Trebuchet MS", "Lucida Sans Unicode", sans-serif;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.workspace :deep(.el-dialog) {
  width: min(780px, calc(100vw - 32px)) !important;
  max-height: calc(100vh - 32px);
  display: flex;
  flex-direction: column;
  border-radius: 28px;
}

.workspace :deep(.el-dialog__body) {
  overflow: auto;
}

.workspace :deep(.el-dialog__footer) {
  flex-shrink: 0;
}

.workspace :deep(.el-dialog__title) {
  color: var(--ink-900);
  font: 700 1.25rem/1.2 Georgia, "Times New Roman", serif;
}

.workspace :deep(.el-select__wrapper),
.workspace :deep(.el-date-editor.el-input__wrapper) {
  min-height: 48px;
  border-radius: 16px;
}

@media (max-width: 1180px) {
  .workspace {
    grid-template-columns: 1fr;
  }

  .workspace__sidebar {
    position: static;
  }

  .metrics-grid,
  .split-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .dialog-grid,
  .quick-actions__grid {
    grid-template-columns: 1fr;
  }

  .workspace__header,
  .toolbar,
  .dialog-toolbar {
    grid-template-columns: 1fr;
    display: grid;
  }

  .toolbar__search {
    min-width: 0;
  }

  .toolbar__actions,
  .header-actions {
    justify-content: stretch;
  }

  .toolbar__actions > *,
  .header-actions > *,
  .dialog-toolbar > * {
    width: 100%;
  }

  .tab-switcher {
    display: grid;
    grid-template-columns: 1fr;
  }

  .tab-button {
    width: 100%;
  }

  .pagination-wrap {
    justify-content: center;
    overflow-x: auto;
  }
}

@media (max-width: 640px) {
  .workspace {
    gap: 14px;
    padding: 12px;
  }

  .workspace__sidebar,
  .data-card,
  .panel-soft {
    border-radius: 22px;
  }

  .workspace__sidebar {
    gap: 10px;
    padding: 14px;
  }

  .data-card,
  .toolbar,
  .insight-panel,
  .quick-actions {
    padding: 12px;
  }

  .workspace__header h2 {
    font-size: 1.85rem;
  }

  .brand-block h1 {
    font-size: 1.65rem;
  }

  .nav-button {
    padding: 12px;
  }

  .primary-button,
  .ghost-button,
  .upload-button {
    width: 100%;
    min-height: 44px;
  }

  .form-images {
    align-items: flex-start;
  }

  .workspace :deep(.el-dialog) {
    width: calc(100vw - 20px) !important;
    max-height: calc(100vh - 20px);
    margin-top: 10px !important;
    margin-bottom: 10px !important;
  }

  .workspace :deep(.el-dialog__body),
  .workspace :deep(.el-dialog__header),
  .workspace :deep(.el-dialog__footer) {
    padding-left: 14px;
    padding-right: 14px;
  }
}
</style>
