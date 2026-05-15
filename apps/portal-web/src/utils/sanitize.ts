const BLOCKED_TAGS = /<\/?(script|iframe|object|embed|link|meta|style|base|form|input|button|textarea|select)[^>]*>/gi
const EVENT_ATTRIBUTES = /\s+on[a-z]+\s*=\s*(".*?"|'.*?'|[^\s>]+)/gi
const DANGEROUS_URLS = /\s+(href|src)\s*=\s*("|\')?\s*javascript:[^"'\s>]*/gi

export const sanitizeHtml = (html?: string | null) =>
  (html ?? '')
    .replace(BLOCKED_TAGS, '')
    .replace(EVENT_ATTRIBUTES, '')
    .replace(DANGEROUS_URLS, '')
