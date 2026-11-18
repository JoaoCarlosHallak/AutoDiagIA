const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()
const API_BASE_URL = configuredBaseUrl
  ? configuredBaseUrl.replace(/\/+$/, '')
  : '/api'

function getErrorMessage(body: unknown): string | undefined {
  if (typeof body === 'string' && body.trim()) {
    return body
  }

  if (
    typeof body === 'object' &&
    body !== null &&
    'message' in body &&
    typeof body.message === 'string'
  ) {
    return body.message
  }

  return undefined
}

async function readBody(response: Response): Promise<unknown> {
  const text = await response.text()
  if (!text) {
    return undefined
  }

  try {
    return JSON.parse(text) as unknown
  } catch {
    return text
  }
}

export async function requestApi(
  path: string,
  init?: RequestInit,
): Promise<unknown> {
  const url = `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
  let response: Response

  try {
    response = await fetch(url, init)
  } catch (error) {
    const reason = error instanceof Error ? ` (${error.message})` : ''
    throw new Error(
      `Não foi possível acessar a API em ${url}. Verifique se o backend está em execução e confira VITE_API_BASE_URL.${reason}`,
      { cause: error },
    )
  }

  const body = await readBody(response)
  if (!response.ok) {
    const detail = getErrorMessage(body)
    throw new Error(
      detail
        ? `${detail} (HTTP ${response.status})`
        : `A requisição falhou (HTTP ${response.status}).`,
    )
  }

  return body
}
