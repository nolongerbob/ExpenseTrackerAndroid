# 🚀 Быстрый старт Android проекта

## Если Android Studio не распознает проект:

### Вариант 1: Импорт проекта
1. В Android Studio выберите **File → Open**
2. Выберите папку `ExpenseTrackerAndroid`
3. Android Studio спросит: **"Open as Project"** или **"Import Gradle Project"** - выберите **"Open as Project"**
4. Дождитесь синхронизации Gradle

### Вариант 2: Синхронизация вручную
1. Если проект открыт, но ничего не происходит:
   - Нажмите **File → Sync Project with Gradle Files**
   - Или нажмите кнопку **"Sync Now"** в верхней панели (если появится)

### Вариант 3: Переоткрытие проекта
1. **File → Close Project**
2. **File → Open** → выберите папку `ExpenseTrackerAndroid`
3. Дождитесь индексации и синхронизации

## Что должно произойти:

1. ✅ Android Studio начнет синхронизацию Gradle (внизу будет прогресс-бар)
2. ✅ Загрузятся все зависимости (может занять 2-5 минут)
3. ✅ Проект будет проиндексирован
4. ✅ В левой панели появится структура проекта

## Если ничего не помогает:

1. **Invalidate Caches:**
   - **File → Invalidate Caches / Restart**
   - Выберите **"Invalidate and Restart"**

2. **Проверьте JDK:**
   - **File → Project Structure → SDK Location**
   - Убедитесь, что JDK 17 установлен

3. **Проверьте Gradle:**
   - **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
   - Убедитесь, что используется **Gradle wrapper**

## Структура проекта должна выглядеть так:

```
ExpenseTrackerAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/expensetracker/
│   │   └── res/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```

## После успешной синхронизации:

1. Создайте виртуальное устройство (AVD):
   - **Tools → Device Manager → Create Device**
   - Выберите любой телефон (например, Pixel 5)
   - Выберите систему (например, Android 13)

2. Запустите приложение:
   - Нажмите зеленую кнопку **Run** (▶️)
   - Или **Shift + F10**


