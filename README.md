# ExpenseTracker Android

Android версия приложения ExpenseTracker, построенная с использованием Jetpack Compose и Material Design 3.

## 🚀 Особенности

- ✅ Полная совместимость аккаунтов с iOS версией
- ✅ Современный UI с Material Design 3
- ✅ Jetpack Compose для нативного UI
- ✅ Offline-first архитектура с синхронизацией
- ✅ Все функции iOS версии:
  - Учет расходов и доходов
  - Категории с иконками и цветами
  - Заметки с напоминаниями
  - Социальная лента (посты, лайки, комментарии)
  - Друзья и лидерборд
  - Темная/светлая тема

## 📋 Требования

- Android Studio Hedgehog (2023.1.1) или новее
- JDK 17
- Android SDK 24+ (минимум Android 7.0)
- Target SDK 34 (Android 14)

## 🛠 Установка

1. Откройте проект в Android Studio
2. Дождитесь синхронизации Gradle
3. Запустите на эмуляторе или реальном устройстве

## 📁 Структура проекта

```
app/src/main/java/com/expensetracker/
├── data/
│   ├── models/          # Модели данных
│   ├── remote/          # API сервисы
│   ├── local/           # Локальное хранилище (Room)
│   └── repository/      # Репозитории
├── ui/
│   ├── auth/            # Экран авторизации
│   ├── dashboard/       # Главный экран
│   ├── expenses/        # Экран расходов
│   ├── notes/           # Экран заметок
│   ├── profile/         # Экран профиля
│   └── onboarding/      # Онбординг
├── util/                # Утилиты
└── di/                  # Dependency Injection (Hilt)
```

## 🔧 Технологии

- **Jetpack Compose** - современный UI toolkit
- **Material Design 3** - дизайн система
- **Retrofit** - HTTP клиент
- **Room** - локальная база данных
- **DataStore** - хранение настроек
- **Coil** - загрузка изображений
- **Navigation Compose** - навигация
- **ViewModel** - управление состоянием
- **Coroutines** - асинхронность
- **Kotlinx Serialization** - сериализация

## 🌐 API

Приложение использует тот же сервер, что и iOS версия:
- Base URL: `https://expense-tracker-api-sbxx.onrender.com`
- Все эндпоинты идентичны iOS версии

## 🎨 Дизайн

Приложение следует Material Design 3 guidelines:
- Адаптивные цвета (Material You)
- Анимации и переходы
- Адаптивные layouts для разных размеров экранов
- Поддержка темной и светлой темы

## 📝 TODO

- [ ] Завершить реализацию всех экранов
- [ ] Добавить Unit тесты
- [ ] Добавить UI тесты
- [ ] Оптимизировать производительность
- [ ] Добавить поддержку нескольких языков

## 📄 Лицензия

См. LICENSE файл




