# Приложение MEGA Kazino

## О проекте
Mega Kazino — мобильное Android-приложение, несмотря на свое название, являющиееся интернет магазином.

## Платформа и стек
- Платформа: Android
- Язык: Kotlin
- UI: Android Views + XML
- Сборка: Gradle (Kotlin DSL)
- IDE: Android Studio

## Состав команды
- Онищенко Александр (`nightmareunderpants`) — Lead Android Developer, UI/UX Designer, Build & Release, Project Manager
- Калюжко Алексей (`Aleshka228PRO`) — Senior Android Developer
- Троянов Михаил (`papaChill`) — Junior Android Developer

## Как запустить проект

### 1. Клонировать репозиторий
```bash
git clone https://github.com/FEIP-FEFU-Mobile-Spring-2026/team-tupie-kozirki
cd team-tupie-kozirki
```

### 2. Открыть проект
Откройте корневую папку проекта в Android Studio: `team-tupie-kozirki`

### 3. Проверить окружение
Для сборки понадобятся:

JDK 17<br>
Android Studio<br>
Android SDK<br>
~~Gradle Wrapper~~, уже включён в проект

### 4. Синхронизировать Gradle
После открытия проекта дождитесь `Gradle Sync`.

### 5. Собрать проект
Вариант через Android Studio:
`Build -> Make Project`

Вариант через терминал Windows:
```bash
gradlew.bat assembleDebug
```

6. Запустить приложение
Создайте или выберите Android Emulator в `Device Manager`
Нажмите `Run` в Android Studio