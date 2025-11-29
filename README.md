# 🎬 Content Advisor - AI-Powered Movie & TV Series Recommendation App

Content Advisor is an AI-powered Android application that provides movie and TV series recommendations to users. It fetches current content information using the TMDB API and provides personalized recommendations through OpenAI ChatGPT.

## ✨ Features

- 🎭 **Movie & TV Series Discovery**: Discover popular movies and TV series
- 🔍 **Advanced Search**: Search for movies, TV series, and actors
- 🤖 **AI Chatbot**: Get movie/TV series recommendations with ChatGPT
- 👤 **User Profile**: Save and track your watched content
- 🎯 **Personalized Recommendations**: Get recommendations based on your watch history
- 📱 **Modern UI**: User-friendly interface designed with Material Design
- 🔐 **Secure Authentication**: User management with Firebase

## 🛠️ Technologies

### Backend & APIs
- **Firebase Realtime Database**: User data and session management
- **TMDB API**: Movie and TV series data
- **OpenAI ChatGPT API**: AI-powered recommendations

### Architecture & Libraries
- **MVVM Pattern**: Model-View-ViewModel architecture
- **Kotlin Coroutines**: Asynchronous programming
- **Retrofit**: HTTP client and API integration
- **Glide**: Image loading and caching
- **Material Design Components**: Modern UI components
- **ViewBinding**: Type-safe view references
- **LiveData & ViewModel**: Reactive data management

### Development Tools
- **Android Studio**: IDE
- **Gradle**: Build system
- **Kotlin**: Programming language

## 📋 Requirements

- Android Studio Hedgehog | 2023.1.1 or higher
- JDK 11 or higher
- Android SDK 24 (Android 7.0) or higher
- Minimum Android 7.0 (API 24)
- Target Android 14 (API 35)

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/Content_Advisor.git
cd Content_Advisor
```

### 2. Configure API Keys

#### TMDB API Key
1. Create an account on [TMDB](https://www.themoviedb.org/)
2. Get your API Key from Settings > API section
3. Add it to `local.properties` file:

```properties
TMDB_API_KEY=your_tmdb_api_key_here
```

#### OpenAI API Key
1. Create an account on [OpenAI Platform](https://platform.openai.com/)
2. Create a new API key from API Keys section
3. Add it to `local.properties` file:

```properties
OPENAI_API_KEY=your_openai_api_key_here
```

#### Firebase Configuration
1. Create a project on [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app (package name: `com.example.content_advisor`)
3. Download `google-services.json` and place it in the `app/` folder

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run the Application

Open the project in Android Studio and click Run, or:

```bash
./gradlew installDebug
```

## 📁 Project Structure

```
app/src/main/java/com/example/content_advisor/
├── api/                    # API services and Retrofit clients
│   ├── GptApiService.kt
│   ├── GptRetrofitClient.kt
│   ├── RetrofitClient.kt
│   └── TMDBApiService.kt
├── adapter/                # RecyclerView adapters
│   ├── PosterAdapter.kt
│   └── SearchAdapter.kt
├── model/                  # Data model classes
│   ├── Credits.kt
│   ├── GptRequest.kt
│   ├── GptResponse.kt
│   ├── SearchResult.kt
│   └── TMDBResponse.kt
├── repository/             # Repository pattern implementation
│   ├── GptRepository.kt
│   └── MovieRepository.kt
├── viewmodel/              # ViewModel classes
│   ├── ChatViewModel.kt
│   └── MovieViewModel.kt
├── ChatActivity.kt         # AI chatbot page
├── DetailActivity.kt       # Movie/TV series detail page
├── LoginPageActivity.kt    # Login page
├── MainActivity.kt         # Launcher activity
├── MainPageActivity.kt     # Main page
├── ProfilePageActivity.kt  # Profile page
└── RegisterPageActivity.kt # Registration page
```

## 🎯 Usage

### User Registration and Login
1. Open the application
2. Click "Not registered yet?" link to register
3. Enter your email, name, and password
4. You will be automatically logged in after registration

### Discovering Movies/TV Series
- Popular movies and TV series are displayed on the home page
- Use the search bar to search for content
- Click on any content to view its detail page

### Using AI Chatbot
1. Click the Chat icon from the bottom navigation
2. Ask questions about movies or TV series
3. Example questions:
   - "Can you recommend comedy movies?"
   - "What shows are similar to Breaking Bad?"
   - "What are Christopher Nolan's best movies?"

### Profile Management
- View your watched content on the profile page
- Add new movies/TV series
- View personalized recommendations
- Log out

## 🔒 Security

- API keys are stored in `local.properties` file (not committed to Git)
- `google-services.json` file is in `.gitignore`
- User passwords are securely stored in Firebase
- Session management with SharedPreferences

## 🙏 Acknowledgments

- [The Movie Database (TMDB)](https://www.themoviedb.org/) - For movie and TV series data
- [OpenAI](https://openai.com/) - For ChatGPT API
- [Firebase](https://firebase.google.com/) - For backend services

---

