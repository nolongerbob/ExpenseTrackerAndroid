# 📱 Настройка Android проекта ExpenseTracker

## ✅ Что уже создано

### 1. Базовая структура проекта
- ✅ Gradle конфигурация (build.gradle.kts)
- ✅ AndroidManifest.xml с разрешениями
- ✅ Структура папок
- ✅ .gitignore

### 2. Модели данных
- ✅ `Expense` - расходы/доходы
- ✅ `Category` - категории
- ✅ `UserProfile` - профиль пользователя
- ✅ `Post`, `Like`, `Comment` - социальная лента
- ✅ `Note` - заметки
- ✅ `Friend` - друзья

### 3. API сервис
- ✅ `ApiService` - интерфейс Retrofit со всеми эндпоинтами
- ✅ `RetrofitClient` - конфигурация Retrofit
- ✅ Все Request/Response модели

### 4. UI тема
- ✅ Material Design 3 тема
- ✅ Поддержка темной/светлой темы
- ✅ Адаптивные цвета (Material You)

## 🔧 Что нужно доделать

### 1. Исправить модели данных
Модели используют `@Serializable`, но нужно:
- Убрать `@Serializable` из моделей (используем Gson для Retrofit)
- Или перейти на kotlinx.serialization полностью

**Рекомендация**: Оставить Gson, так как он проще для работы с Retrofit.

### 2. Создать ViewModels
Нужно создать ViewModels для каждого экрана:
- `AuthViewModel` - авторизация
- `DashboardViewModel` - главный экран
- `ExpensesViewModel` - расходы
- `NotesViewModel` - заметки
- `ProfileViewModel` - профиль

### 3. Создать UI экраны
Экраны с Jetpack Compose:
- `OnboardingScreen` - онбординг
- `LoginScreen` - вход
- `RegisterScreen` - регистрация
- `DashboardScreen` - главный экран
- `ExpensesScreen` - список расходов
- `AddExpenseScreen` - добавление расхода
- `EditExpenseScreen` - редактирование расхода
- `NotesScreen` - список заметок
- `AddNoteScreen` - добавление заметки
- `ProfileScreen` - профиль
- `SettingsScreen` - настройки

### 4. Настроить навигацию
- Создать `NavGraph` с маршрутами
- Настроить навигацию между экранами
- Добавить bottom navigation для главных экранов

### 5. Локальное хранилище (Room)
- Создать Entity классы
- Создать DAO интерфейсы
- Создать Database класс
- Настроить миграции

### 6. DataStore для настроек
- Создать Preferences DataStore
- Хранить токен авторизации
- Хранить настройки темы
- Хранить флаг завершения онбординга

### 7. TokenManager
Исправить `TokenManager` в `RetrofitClient.kt`:
- Использовать DataStore вместо прямого доступа
- Инжектировать через DI (Hilt или ручной DI)

### 8. Синхронизация
- Создать `SyncService` для фоновой синхронизации
- Использовать WorkManager для периодической синхронизации

## 🚀 Быстрый старт

1. Откройте проект в Android Studio
2. Дождитесь синхронизации Gradle
3. Исправьте ошибки компиляции (если есть)
4. Начните с создания ViewModels и UI экранов

## 📝 Примечания

- Все модели данных должны быть совместимы с iOS версией
- API эндпоинты идентичны iOS версии
- Используйте Material Design 3 компоненты
- Следуйте Android best practices

## 🔗 Полезные ссылки

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Retrofit](https://square.github.io/retrofit/)
- [Room](https://developer.android.com/training/data-storage/room)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)


