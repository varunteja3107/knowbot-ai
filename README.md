# 🤖 KnowBot AI

### Your Personal AI-Powered Study and Productivity Assistant

KnowBot AI is a Java desktop application designed to help students organize their academic life, manage study materials, practice quizzes, and interact with an AI assistant in one place.

Built with Java Swing, Maven, SQLite/JDBC, and local AI integration through Ollama, KnowBot AI brings learning and productivity tools together in a simple desktop experience.

---

## ✨ Features

### 📚 Study Management
- **My Notes:** Create and organize personal study notes.
- **Document Management:** Keep study materials organized.
- **Study Planner:** Plan study activities and academic goals.
- **Quiz Practice:** Practice quizzes and review previous quiz activity.

### 🧠 AI Assistant
- **AI-Powered Assistance:** Interact with a locally configured language model.
- **Local AI Integration:** Connect to Ollama using the `llama3.2:1b` model.
- **Learning Support:** Use the assistant to explore questions and study topics.

### 📊 Productivity and Insights
- **Analytics Dashboard:** View available study and productivity insights.
- **Central Dashboard:** Access application features from a single interface.
- **Local Data Storage:** Store application data in a local SQLite database.

*Feature availability depends on the current implementation. Future improvements are tracked in the roadmap below.*

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Core application logic |
| Java Swing | Desktop graphical user interface |
| Maven | Build and dependency management |
| SQLite | Local database storage |
| JDBC | Database connectivity |
| Ollama | Local AI model runtime |
| llama3.2:1b | Configured language model |
| JUnit | Automated testing |

---

## 🏗️ Architecture

```text
┌─────────────────────────────────┐
│          KnowBot AI UI          │
│       Java Swing Dashboard      │
└────────────────┬────────────────┘
                 │
       ┌─────────┼──────────┐
       │         │          │
       ▼         ▼          ▼
   My Notes   Study Tools  Analytics
       │         │          │
       └─────────┼──────────┘
                 ▼
          Application Logic
                 │
          ┌──────┴──────┐
          ▼             ▼
      SQLite/JDBC    Ollama AI
                    llama3.2:1b
```

---

## 🚀 Getting Started

### Prerequisites

Install or configure the following:

- Java Development Kit (JDK) 21
- Apache Maven
- Git
- Ollama, if you want to use the local AI assistant

### 1. Clone the repository

```bash
git clone https://github.com/varunteja3107/knowbot-ai.git
cd knowbot-ai
```

### 2. Check Java and Maven

```bash
java -version
mvn -version
```

Confirm that Java 21 is being used.

### 3. Start the local AI service

Install Ollama from https://ollama.com if it is not already installed.

Download the configured model:

```bash
ollama pull llama3.2:1b
```

Make sure the Ollama service is running before using AI features.

### 4. Build and test

```bash
mvn clean test
```

### 5. Launch KnowBot AI

```bash
mvn exec:java
```

If the Maven execution plugin is not configured in `pom.xml`, use the launch method specified by the project configuration.

---

## 💾 Data Storage

KnowBot AI uses SQLite for local application data.

The development configuration may store the database at:

```text
~/.knowbot-ai/knowbot.db
```

The application may also support a custom database path through a Java system property:

```bash
-Dknowbot.db.path=/path/to/knowbot.db
```

Keep your local database and backups safe. Database files containing personal information should not be committed to GitHub.

---

## 📁 Project Structure

```text
knowbot-ai/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── knowbot/
│   └── test/
├── .gitignore
├── pom.xml
└── README.md
```

The exact source and test directories depend on the current project files.

---

## 🗺️ Roadmap

- [ ] Improve navigation and dashboard responsiveness
- [ ] Refine the notes and study-planning experience
- [ ] Expand quiz and learning analytics
- [ ] Improve error handling and automated test coverage
- [ ] Add application screenshots and a demonstration video
- [ ] Explore a web-based version for access across devices
- [ ] Evaluate secure deployment options for the AI backend

---

## 🔐 Privacy and Security

- Local database files and personal backups should remain outside version control.
- Do not commit API keys, passwords, private documents, or credentials.
- Local AI execution depends on the installed Ollama service and model.
- A future hosted version will require a separate web interface and deployment architecture.

---

## 🧪 Testing

Run the project tests with:

```bash
mvn clean test
```

Testing results should be verified against the current source code and environment.

---

## 👨‍💻 Author

**Varun Teja**

Computer Science and Engineering student interested in Java development, artificial intelligence, and productivity tools.

GitHub: [@varunteja3107](https://github.com/varunteja3107)

---

## 📄 License

No license has been specified yet. Add a suitable open-source license before permitting others to reuse, modify, or distribute this project.

---

⭐ If you find this project interesting, consider starring the repository!
