import { useEffect, useState } from "react";
import "./App.css";

const initialNotes = [
  { id: 1, title: "Operating Systems", content: "Review CPU scheduling, process states, and system calls.", date: "Today" },
  { id: 2, title: "Artificial Intelligence", content: "Study supervised learning, classification, and model evaluation.", date: "Yesterday" },
];

const initialTasks = [
  { id: 1, title: "Complete DSA revision", subject: "Computer Science", date: "Today", done: false },
  { id: 2, title: "Review project documentation", subject: "KnowBot AI", date: "Tomorrow", done: false },
  { id: 3, title: "Practice Java concepts", subject: "Programming", date: "This week", done: true },
];

const initialDocs = [
  { id: 1, name: "DSA Study Material.pdf", type: "PDF document" },
  { id: 2, name: "Java Revision Notes.txt", type: "Text document" },
];

const menu = [
  ["Overview", "◫"], ["Notes", "▤"], ["Documents", "▧"],
  ["Quizzes", "◎"], ["Study Planner", "☷"], ["AI Assistant", "✳"],
];

function readStore(key, fallback) {
  try {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : fallback;
  } catch {
    return fallback;
  }
}

export default function App() {
  const [page, setPage] = useState("Overview");
  const [notes, setNotes] = useState(() => readStore("knowbot-notes", initialNotes));
  const [tasks, setTasks] = useState(() => readStore("knowbot-tasks", initialTasks));
  const [docs, setDocs] = useState(() => readStore("knowbot-docs", initialDocs));
  const [noteSearch, setNoteSearch] = useState("");
  const [taskTitle, setTaskTitle] = useState("");
  const [noteTitle, setNoteTitle] = useState("");
  const [noteContent, setNoteContent] = useState("");
  const [chatInput, setChatInput] = useState("");
  const [messages, setMessages] = useState([
    { role: "assistant", text: "Hi! I'm KnowBot. Ask me a question, or tell me what you're studying today." },
  ]);
  const [quizStarted, setQuizStarted] = useState(false);
  const [quizAnswer, setQuizAnswer] = useState("");
  const [quizFeedback, setQuizFeedback] = useState("");
  const [apiStatus, setApiStatus] = useState("Checking");

  useEffect(() => { localStorage.setItem("knowbot-notes", JSON.stringify(notes)); }, [notes]);
  useEffect(() => { localStorage.setItem("knowbot-tasks", JSON.stringify(tasks)); }, [tasks]);
  useEffect(() => { localStorage.setItem("knowbot-docs", JSON.stringify(docs)); }, [docs]);

  useEffect(() => {
    fetch("http://localhost:8080/api/health")
      .then((r) => { if (!r.ok) throw new Error(); return r.json(); })
      .then(() => setApiStatus("Connected"))
      .catch(() => setApiStatus("Offline"));
  }, []);

  function addNote(e) {
    e.preventDefault();
    if (!noteTitle.trim() || !noteContent.trim()) return;
    setNotes([{ id: Date.now(), title: noteTitle.trim(), content: noteContent.trim(), date: "Just now" }, ...notes]);
    setNoteTitle("");
    setNoteContent("");
  }

  function addTask(e) {
    e.preventDefault();
    if (!taskTitle.trim()) return;
    setTasks([{ id: Date.now(), title: taskTitle.trim(), subject: "Personal", date: "Upcoming", done: false }, ...tasks]);
    setTaskTitle("");
  }

  function uploadDocument(e) {
    const files = Array.from(e.target.files || []);
    if (files.length) {
      setDocs([...files.map((f) => ({ id: Date.now() + Math.random(), name: f.name, type: f.type || "File" })), ...docs]);
    }
    e.target.value = "";
  }

  function sendMessage(e) {
    e.preventDefault();
    const question = chatInput.trim();
    if (!question) return;
    setMessages((old) => [
      ...old,
      { role: "user", text: question },
      { role: "assistant", text: "I'm currently running in demo mode. The chat interface is ready, but AI answers will work after we connect an AI service to the backend." },
    ]);
    setChatInput("");
  }

  const completed = tasks.filter((t) => t.done).length;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">K</div>
          <div><strong>knowbot<span>AI</span></strong><small>Your learning space</small></div>
        </div>
        <div className="nav-label">WORKSPACE</div>
        <nav>
          {menu.map(([name, icon]) => (
            <button key={name} className={`nav-item ${page === name ? "active" : ""}`} onClick={() => setPage(name)}>
              <span className="nav-icon">{icon}</span>{name}
              {name === "AI Assistant" && <span className="new-tag">AI</span>}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="status-dot" />
          <div><strong>Java API</strong><small>{apiStatus}</small></div>
          <span className={`status-pill ${apiStatus === "Connected" ? "online" : ""}`}>●</span>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="breadcrumb">Workspace <span>/</span> <strong>{page}</strong></div>
          <div className="profile"><div className="avatar">K</div><div><strong>My workspace</strong><small>Student account</small></div></div>
        </header>

        <div className="page-content">
          {page === "Overview" && <>
            <section className="welcome">
              <div><div className="eyebrow">YOUR PERSONAL LEARNING HUB</div><h1>Make today count.</h1><p>Your ideas, study plans, and learning progress, all in one place.</p></div>
              <div className="welcome-mark">✳</div>
            </section>
            <section className="stats-grid">
              <Stat label="TOTAL NOTES" value={notes.length} icon="▤" detail="Ideas worth keeping" />
              <Stat label="DOCUMENTS" value={docs.length} icon="▧" detail="Your study library" />
              <Stat label="STUDY TASKS" value={tasks.length} icon="☷" detail={`${completed} completed`} />
              <Stat label="COMPLETION" value={`${tasks.length ? Math.round(completed / tasks.length * 100) : 0}%`} icon="↗" detail="Task progress" />
            </section>
            <div className="content-grid">
              <section className="panel">
                <div className="panel-heading"><div><h2>Today's focus</h2><p>Small steps add up.</p></div><button className="text-button" onClick={() => setPage("Study Planner")}>View planner ↗</button></div>
                {tasks.slice(0, 4).map((task) => <TaskRow key={task.id} task={task} onToggle={() => setTasks(tasks.map((t) => t.id === task.id ? { ...t, done: !t.done } : t))} />)}
                <form className="inline-add" onSubmit={addTask}><input value={taskTitle} onChange={(e) => setTaskTitle(e.target.value)} placeholder="Add a new study task..." /><button type="submit">+ Add</button></form>
              </section>
              <section className="panel">
                <div className="panel-heading"><div><h2>Recent notes</h2><p>Pick up where you left off.</p></div><button className="text-button" onClick={() => setPage("Notes")}>All notes ↗</button></div>
                {notes.slice(0, 3).map((note) => <button className="note-preview" key={note.id} onClick={() => setPage("Notes")}><div className="note-symbol">▤</div><div><strong>{note.title}</strong><p>{note.content}</p><small>{note.date}</small></div></button>)}
                <button className="secondary-button full-button" onClick={() => setPage("Notes")}>＋ Create a note</button>
              </section>
            </div>
            <section className="tip-banner"><div className="tip-icon">✳</div><div><strong>A little learning, every day.</strong><p>Choose one task, focus for 25 minutes, and give yourself credit for showing up.</p></div><button onClick={() => setPage("AI Assistant")}>Ask KnowBot ↗</button></section>
          </>}

          {page === "Notes" && <section className="panel full-panel">
            <PageTitle title="Your notes" subtitle="Capture ideas before they disappear." />
            <form className="note-form" onSubmit={addNote}><input value={noteTitle} onChange={(e) => setNoteTitle(e.target.value)} placeholder="Note title" required /><textarea value={noteContent} onChange={(e) => setNoteContent(e.target.value)} placeholder="Write your thoughts..." rows="4" required /><button className="primary-button">Save note</button></form>
            <input className="search-input" value={noteSearch} onChange={(e) => setNoteSearch(e.target.value)} placeholder="⌕  Search your notes..." />
            <div className="notes-grid">{notes.filter((n) => `${n.title} ${n.content}`.toLowerCase().includes(noteSearch.toLowerCase())).map((n) => <article className="note-card" key={n.id}><div className="note-card-top"><span>▤ NOTE</span><button className="delete-button" onClick={() => setNotes(notes.filter((x) => x.id !== n.id))}>Delete</button></div><h3>{n.title}</h3><p>{n.content}</p><small>{n.date}</small></article>)}</div>
          </section>}

          {page === "Documents" && <section className="panel full-panel">
            <PageTitle title="Document library" subtitle="Keep your learning resources together." />
            <label className="upload-box"><span className="upload-symbol">↑</span><strong>Choose files to add</strong><span>Pick documents from your Mac to list them here.</span><input type="file" multiple onChange={uploadDocument} /></label>
            <div className="document-list">{docs.map((doc) => <div className="document-row" key={doc.id}><div className="doc-icon">▧</div><div className="document-info"><strong>{doc.name}</strong><small>{doc.type}</small></div><button className="delete-button" onClick={() => setDocs(docs.filter((d) => d.id !== doc.id))}>Remove</button></div>)}</div>
            <p className="muted-note">Demo limitation: files are listed in this browser; their contents are not uploaded to the Java backend yet.</p>
          </section>}

          {page === "Quizzes" && <section className="panel full-panel">
            <PageTitle title="Knowledge check" subtitle="A quick question to keep your mind sharp." />
            <div className="quiz-card"><div className="eyebrow">JAVA FUNDAMENTALS · QUESTION 1</div><h2>Which concept allows a Java class to inherit fields and methods from another class?</h2>{["Encapsulation", "Inheritance", "Polymorphism", "Abstraction"].map((option) => <button key={option} className={`quiz-option ${quizAnswer === option ? "selected" : ""}`} onClick={() => { setQuizAnswer(option); setQuizFeedback(""); }}>{option}<span>{quizAnswer === option ? "●" : "○"}</span></button>)}<button className="primary-button" disabled={!quizAnswer} onClick={() => { setQuizStarted(true); setQuizFeedback(quizAnswer === "Inheritance" ? "Correct! Inheritance lets a class derive fields and methods from another class." : "Not quite. The correct answer is Inheritance."); }}>Check answer</button>{quizStarted && <p className="quiz-feedback">{quizFeedback}</p>}</div>
          </section>}

          {page === "Study Planner" && <section className="panel full-panel">
            <PageTitle title="Study planner" subtitle="Turn your goals into manageable steps." />
            <form className="inline-add planner-add" onSubmit={addTask}><input value={taskTitle} onChange={(e) => setTaskTitle(e.target.value)} placeholder="What do you want to study?" /><button type="submit">+ Add task</button></form>
            <div className="planner-summary"><div><span>Total tasks</span><strong>{tasks.length}</strong></div><div><span>Completed</span><strong>{completed}</strong></div><div><span>Remaining</span><strong>{tasks.length - completed}</strong></div></div>
            {tasks.map((task) => <TaskRow key={task.id} task={task} onToggle={() => setTasks(tasks.map((t) => t.id === task.id ? { ...t, done: !t.done } : t))} onDelete={() => setTasks(tasks.filter((t) => t.id !== task.id))} />)}
          </section>}

          {page === "AI Assistant" && <section className="panel chat-panel">
            <PageTitle title="KnowBot AI assistant" subtitle="Your study companion, ready for the next question." />
            <div className="chat-messages">{messages.map((m, i) => <div className={`chat-message ${m.role}`} key={i}><div className="chat-avatar">{m.role === "assistant" ? "K" : "Y"}</div><div><small>{m.role === "assistant" ? "KNOWBOT" : "YOU"}</small><p>{m.text}</p></div></div>)}</div>
            <form className="chat-form" onSubmit={sendMessage}><input value={chatInput} onChange={(e) => setChatInput(e.target.value)} placeholder="Ask a study question..." /><button className="primary-button">Send ↑</button></form>
            <p className="muted-note">Demo mode: real AI responses are not connected yet.</p>
          </section>}
          <footer>KnowBot AI <span>•</span> Your learning space <span>•</span> Web preview</footer>
        </div>
      </main>
    </div>
  );
}

function Stat({ label, value, icon, detail }) {
  return <div className="stat-card"><div className="stat-top"><span>{label}</span><span className="stat-icon">{icon}</span></div><strong className="stat-value">{value}</strong><small>{detail}</small></div>;
}
function PageTitle({ title, subtitle }) {
  return <div className="page-title"><div><h1>{title}</h1><p>{subtitle}</p></div></div>;
}
function TaskRow({ task, onToggle, onDelete }) {
  return <div className={`task-row ${task.done ? "task-done" : ""}`}><button className="check-button" onClick={onToggle} aria-label={task.done ? "Mark incomplete" : "Mark complete"}>{task.done ? "✓" : ""}</button><div className="task-info"><strong>{task.title}</strong><small>{task.subject} · {task.date}</small></div>{onDelete && <button className="delete-button" onClick={onDelete}>Delete</button>}</div>;
}
