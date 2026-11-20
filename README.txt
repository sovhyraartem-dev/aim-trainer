Aim Trainer (Fabric 1.21.4, клиентский мод)

СБОРКА
Вариант 1 (проще): на https://fabricmc.net/develop/template/ сгенерируй шаблон для 1.21.4
(Yarn, Fabric API, без split sources), замени его папку src и файл fabric.mod.json на мои,
затем выполни:  gradlew build   -> готовый .jar будет в build/libs/

Вариант 2: если установлен Gradle 8.12+ и Java 21, в этой папке выполни:
  gradle wrapper --gradle-version 8.12
  gradlew build

УСТАНОВКА
Положи .jar (не -sources) в папку mods вместе с Fabric API.

ИСПОЛЬЗОВАНИЕ
L - открыть меню (Старт, режим, размер, количество, время).
ЛКМ - стрелять по виртуальным шарикам во время раунда.
Шарики видны только тебе и ни на что в мире не влияют.

СБОРКА БЕЗ УСТАНОВКИ ЧЕГО-ЛИБО (через GitHub)
1. Создай аккаунт на github.com, нажми New repository (любое имя), затем "uploading an existing file".
2. Распакуй этот архив и загрузи ВСЁ содержимое (вместе с папкой .github) в репозиторий.
3. Открой вкладку Actions, дождись зелёной галочки (1-3 мин).
4. Открой запуск, внизу в Artifacts скачай aim-trainer-jar, внутри будет aim-trainer-1.0.0.jar.
5. Положи jar в папку mods (вместе с Fabric API для 1.21.4).
