const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')

const renderInline = (value: string) =>
  escapeHtml(value)
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+?)`/g, '<code>$1</code>')

const closeList = (html: string[], listType: 'ul' | 'ol' | null) => {
  if (listType) {
    html.push(`</${listType}>`)
  }
  return null
}

export const renderAiMessageHtml = (content?: string | null) => {
  const text = (content ?? '').trim()
  if (!text) {
    return ''
  }

  const html: string[] = []
  let listType: 'ul' | 'ol' | null = null
  const lines = text
    .replace(/\r\n/g, '\n')
    .replace(/([^\n])(\d+\.\s+\*\*)/g, '$1\n$2')
    .replace(/([^\n])(-\s+\*\*)/g, '$1\n$2')
    .split('\n')

  for (const rawLine of lines) {
    const line = rawLine.trim()
    if (!line) {
      listType = closeList(html, listType)
      continue
    }

    const heading = line.match(/^(#{1,3})\s+(.+)$/)
    if (heading) {
      listType = closeList(html, listType)
      const level = heading[1].length + 2
      html.push(`<h${level}>${renderInline(heading[2])}</h${level}>`)
      continue
    }

    const ordered = line.match(/^\d+[.)]\s+(.+)$/)
    if (ordered) {
      if (listType !== 'ol') {
        listType = closeList(html, listType)
        html.push('<ol>')
        listType = 'ol'
      }
      html.push(`<li>${renderInline(ordered[1])}</li>`)
      continue
    }

    const unordered = line.match(/^[-*]\s+(.+)$/)
    if (unordered) {
      if (listType !== 'ul') {
        listType = closeList(html, listType)
        html.push('<ul>')
        listType = 'ul'
      }
      html.push(`<li>${renderInline(unordered[1])}</li>`)
      continue
    }

    listType = closeList(html, listType)
    html.push(`<p>${renderInline(line)}</p>`)
  }

  closeList(html, listType)
  return html.join('')
}
