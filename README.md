# VChat

Приватный серверный мод ValorCraft для Forge 1.20.1: локальный и глобальный чат, настраиваемый TAB, LuckPerms и FTB Teams.

## Возможности

- Локальный чат работает только в текущем измерении и заданном радиусе.
- Глобальный чат: `/g <сообщение>` или `!сообщение`.
- Prefix, suffix, primary group и weight LuckPerms.
- Форматы локального чата, глобального чата и строки игрока в TAB.
- Сортировка TAB по weight основной группы.
- HEX и стили с отдельными permissions.
- Антиспам, ограничение длины и блокировка повторов.
- Упоминания `@Ник` с подсветкой и звуком.
- Постоянный `/ignore`, сохраняемый в `config/vchat-ignore.json`.
- Безопасное логирование команд без аргументов по умолчанию.
- Диагностика LuckPerms и форматирования через `/vchat debug`.
- Цветная информация FTB Teams при наведении курсора на ник в чате.
- Скрытие голов Chat Heads в ванильных сообщениях смерти без изменения их текста.

## Команды

| Команда | Кто может использовать | Описание |
|---|---|---|
| `/g <сообщение>` | Все | Глобальный чат |
| `/ignore <игрок>` | Все | Добавить или убрать игрока из ignore-списка |
| `/ignore` | Все | Показать подсказку и размер ignore-списка |
| `/ignore clear` | Все | Очистить ignore-список, включая офлайн-игроков |
| `/vchat reload` | Уровень 2 | Перечитать конфиг и обновить TAB |
| `/vchat status` | Уровень 2 | Показать активные настройки |
| `/vchat debug <игрок>` | Уровень 2 | Показать group, weight, prefix, suffix и permissions |

## Конфиг

При первом запуске создаётся `config/vchat-config.json5`. JSON5 поддерживает комментарии `//`; каждое поле в сгенерированном файле подписано по-русски.

Основная структура:

```json5
{
  "configVersion": 8,

  "tab": {
    "header": "...",
    "footer": "...",
    "joinMessage": "Добро пожаловать на ValorCraft!",
    "playerFormat": "<prefix>&f<name><suffix>&r",
    "updateIntervalTicks": 20
  },

  "chat": {
    "localRadius": 100,
    "enableGlobal": true,
    "enableLocal": true,
    "globalCommand": "g",
    "globalFormat": "&e[G] <prefix>&f<name><suffix>&7: &f<message>",
    "localFormat": "&7[L] <prefix>&f<name><suffix>&7: &f<message>",
    "notifyWhenNoOneHeard": true,
    "noOneHeardMessage": "&7Вас никто не услышал",
    "globalDisabledMessage": "&cГлобальный чат сейчас отключён",
    "localDisabledMessage": "&cЛокальный чат сейчас отключён",

    "playerFormatting": {
      "enabled": true,
      "colorsForEveryone": false,
      "hexForEveryone": false,
      "stylesForEveryone": false,
      "obfuscatedForEveryone": false
    },

    "antiSpam": {
      "enabled": true,
      "maxMessageLength": 256,
      "cooldownMillis": 500,
      "blockRepeatedMessages": true,
      "repeatWindowSeconds": 15,
      "tooLongMessage": "&cСообщение слишком длинное. Максимум: <max> символов",
      "tooFastMessage": "&cНе так быстро. Подождите ещё <seconds> сек.",
      "repeatedMessage": "&cНе повторяйте одно и то же сообщение",
      "emptyMessage": "&cСообщение не может быть пустым"
    },

    "mentions": {
      "enabled": true,
      "highlightFormat": "&e&l@<name>&r&f",
      "playSound": true,
      "sound": "minecraft:entity.experience_orb.pickup",
      "volume": 0.8,
      "pitch": 1.2
    },

    "ignore": {
      "enabled": true,
      "addedMessage": "&7Вы больше не видите сообщения игрока &f<name>",
      "removedMessage": "&7Вы снова видите сообщения игрока &f<name>",
      "disabledMessage": "&cСистема игнорирования отключена",
      "cannotIgnoreSelfMessage": "&cНельзя игнорировать самого себя",
      "usageMessage": "&7Использование: &f/ignore <игрок>&7 или &f/ignore clear&7. В списке: &f<count>",
      "clearedMessage": "&7Список игнорирования очищен. Удалено игроков: &f<count>",
      "commandCooldownMillis": 1000,
      "saveIntervalMillis": 1000,
      "cooldownMessage": "&cНе так быстро. Подождите перед повторным изменением списка"
    },

    "logging": {
      "logChatMessages": true,
      "logCommands": true,
      "includeCommandArguments": false,
      "redactedCommands": ["login", "l", "register", "reg", "changepassword", "cp", "password", "2fa"]
    }
  },

  "luckPerms": {
    "showPrefixes": true,
    "showSuffixes": true,
    "sortTabByWeight": true,
    "higherWeightFirst": true
  },

  "ftbTeams": {
    "showTeamOnNameHover": true,
    "showTeamName": true,
    "showPlayerRank": true,
    "showMemberCount": true,
    "hideHoverWithoutTeam": true,
    "teamLabel": "&7Команда: &f",
    "rankLabel": "&7Роль: &f",
    "membersLabel": "&7Участников: &f",
    "noTeamText": "&7Игрок не состоит в команде"
  },

  "deathMessages": {
    "enabled": true,
    "hidePlayerHeads": true
  }
}
```

После изменения выполните `/vchat reload`. Изменение `globalCommand` требует полного перезапуска, потому что команда регистрируется при запуске сервера.

Конфиги старых версий автоматически обновляются до `configVersion: 8` с сохранением настроек. Старый стандартный cooldown `1000` мс при миграции уменьшается до `500` мс. Старый `vchat-tab.json` остаётся резервной копией. Последняя проверенная конфигурация хранится в `vchat-config.json5.last-good`: при синтаксической ошибке `/vchat reload` отклонит новый файл и продолжит использовать рабочие настройки, а после перезапуска сможет восстановиться из этой копии.

## FTB Teams hover

Если FTB Teams установлен, при наведении на `<name>` или `<display_name>` в сообщении показываются цветное название команды, роль игрока и количество участников. Личные одиночные команды FTB Teams скрываются при `hideHoverWithoutTeam: true`. Интеграция необязательная: без FTB Teams VChat продолжает работать без подсказки.

## Сообщения о смерти и Chat Heads

При `deathMessages.enabled: true` и `hidePlayerHeads: true` VChat добавляет невидимый разделитель в имена жертвы и игрока-убийцы только на время формирования ванильного death-компонента. Chat Heads перестаёт ошибочно считать системную строку сообщением игрока. Перевод, причина смерти, имя оружия и hover предмета сохраняются. Настройку `handleSystemMessages` в Chat Heads можно оставить включённой.

## Placeholders

В `globalFormat`, `localFormat` и `playerFormat` доступны:

| Placeholder | Значение |
|---|---|
| `<prefix>` | Prefix LuckPerms с цветами |
| `<suffix>` | Suffix LuckPerms с цветами |
| `<name>` | Настоящий ник |
| `<display_name>` | Текущее отображаемое имя |
| `<group>` | Primary group LuckPerms |
| `<world>` | Текущее измерение |
| `<channel>` | `global`, `local` или `tab` |
| `<message>` | Текст сообщения |

В header/footer: `%online%`, `%max%`, `%player%`.

## Цвета и permissions

Поддерживаются `&6`, `&l`, `#RRGGBB`, `&#RRGGBB`, `&%23RRGGBB` и прописные варианты вроде `&A`.

| Permission | Возможность |
|---|---|
| `vchat.format.color` | Цвета `&0`–`&f` |
| `vchat.format.hex` | HEX-цвета |
| `vchat.format.style` | Bold, italic, underline, strikethrough, reset |
| `vchat.format.obfuscated` | Эффект `&k` |
| `vchat.antispam.bypass` | Обход cooldown, повторов и ограничения длины |

Параметры `ForEveryone: true` разрешают соответствующее оформление без permission. Операторы уровня 2 имеют все разрешения форматирования и обходят антиспам.

## Логирование

Безопасное значение по умолчанию — `includeCommandArguments: false`: в лог попадает название команды, но не её аргументы. Команды из `redactedCommands` скрывают аргументы всегда, даже если глобальная запись аргументов включена. Поддерживаются также namespaced-варианты вроде `/tiauth:login`.

## Установка

1. Поместить `VChat-1.6.1.jar` в `mods/` сервера Forge 1.20.1.
2. Установить LuckPerms, если нужны префиксы, permissions и сортировка.
3. Перезапустить сервер.
4. Настроить `config/vchat-config.json5` и выполнить `/vchat reload`.
