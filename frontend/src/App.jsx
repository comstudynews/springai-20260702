import { useState } from 'react'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

function App() {
  const [question, setQuestion] = useState('Spring AI를 한 문단으로 설명해 줘.')
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const submitQuestion = async (event) => {
    event.preventDefault()

    const trimmed = question.trim()

    if (!trimmed || loading) return

    setError('')
    setLoading(true)
    setMessages((current) => [...current, { role: 'user', text: trimmed }])
    setQuestion('')

    try {
      const response = await fetch(`${API_BASE_URL}/api/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: trimmed }),
      })

      const data = await response.json().catch(() => ({}))

      if (!response.ok) {
        throw new Error(data.message || `요청 실패: HTTP ${response.status}`)
      }

      setMessages((current) => [
        ...current,
        { role: 'assistant', text: data.answer || '응답 내용이 없습니다.' },
      ])
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : '알 수 없는 오류가 발생했습니다.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="page-shell">
      <section className="chat-card">
        <header>
          <p className="eyebrow">SPRING AI STEP BY STEP</p>
          <h1>Spring AI 튜터</h1>
          <p className="subtitle">
            React에서 Spring Boot REST API를 거쳐 OpenAI 모델을 호출합니다.
          </p>
        </header>

        <div className="message-list" aria-live="polite">
          {messages.length === 0 && (
            <div className="empty-state">
              질문을 입력하고 첫 번째 AI 응답을 확인해 보세요.
            </div>
          )}

          {messages.map((message, index) => (
            <article
              key={`${message.role}-${index}`}
              className={`message ${message.role}`}
            >
              <strong>{message.role === 'user' ? '나' : 'AI'}</strong>
              <p>{message.text}</p>
            </article>
          ))}

          {loading && <div className="loading">답변을 작성하고 있습니다.</div>}
        </div>

        {error && <p className="error-box">{error}</p>}

        <form onSubmit={submitQuestion}>
          <label htmlFor="question">질문</label>
          <textarea
            id="question"
            value={question}
            onChange={(event) => setQuestion(event.target.value)}
            maxLength={2000}
            rows={4}
            placeholder="Spring AI에 관해 질문해 보세요."
          />

          <div className="form-footer">
            <span>{question.length}/2000</span>
            <button type="submit" disabled={loading || !question.trim()}>
              {loading ? '요청 중' : '질문 보내기'}
            </button>
          </div>
        </form>
      </section>
    </main>
  )
}

export default App