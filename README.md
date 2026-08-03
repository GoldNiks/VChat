# VChat

Приватный серверный мод ValorCraft для Forge 1.20.1: локальный и глобальный чат, настраиваемый TAB, префиксы и сортировка LuckPerms.

## Возможности

- Глобальный чат: `/g <сообщение>` или `!сообщение`.
- Локальный чат с настраиваемым радиусом.
- Отдельные форматы локального чата, глобального чата и строки игрока в TAB.
- Prefix, suffix, primary group и weight из LuckPerms.
- Сортировка TAB по weight основной группы.
- HEX, обычные цвета и стили с permissions LuckPerms.
- Перезагрузка настроек без перезапуска сервера.

## Команды

| Команда | Описание |
|---|---|
| `/g <сообщение>` | Отправить сообщение в глобальный чат |
| `/vchat reload` | Перечитать конфиг и сразу обновить TAB |
| `/vchat status` | Показать активные настройки TAB и форматирования |

## Конфиг

При первом запуске создаётся `config/vchat-config.json5`. Формат JSON5 разрешает поясняющие комментарии `//`.

```json5
{
  // Версия структуры конфига. Не изменяйте вручную.
  "configVersion": 2,

  // Настройки внешнего вида TAB.
  "tab": {
    // Сверху TAB. Доступны: %online%, %max%, %player%.
    "header": "\n&6&l&nVChat\n\n&7Игроки: &a%online%\n\n&7&m-----------------",
    // Снизу TAB. Доступны те же подстановки.
    "footer": "&7&m-----------------\n\n&7Баланс: &e0",
    // Личное сообщение после входа.
    "joinMessage": "&aДобро пожаловать на &6&l&nVChat&a!",
    // Строка каждого игрока в TAB.
    "playerFormat": "<prefix>&f<name><suffix>",
    // 20 тиков = примерно 1 секунда.
    "updateIntervalTicks": 20
  },

  // Локальный и глобальный чат.
  "chat": {
    "localRadius": 100,
    "enableGlobal": true,
    "enableLocal": true,
    // g означает команду /g сообщение. Для изменения имени команды нужен restart.
    "globalCommand": "g",
    "globalFormat": "&e[G] <prefix>&f<name><suffix>&7: &f<message>",
    "localFormat": "&7[L] <prefix>&f<name><suffix>&7: &f<message>",
    "notifyWhenNoOneHeard": true,
    "noOneHeardMessage": "&7Вас никто не услышал",

    // Оформление внутри текста, который пишет игрок.
    "playerFormatting": {
      "enabled": true,
      // false требует permission vchat.format.color.
      "colorsForEveryone": false,
      // false требует permission vchat.format.hex.
      "hexForEveryone": false,
      // false требует permission vchat.format.style.
      "stylesForEveryone": false,
      // &k лучше оставить только администрации.
      // false требует permission vchat.format.obfuscated.
      "obfuscatedForEveryone": false
    }
  },

  // Если LuckPerms отсутствует, VChat продолжит работать без префиксов.
  "luckPerms": {
    "showPrefixes": true,
    "showSuffixes": true,
    "sortTabByWeight": true,
    // true: больший weight располагается выше.
    "higherWeightFirst": true
  }
}
```

После изменения файла выполните `/vchat reload`. Конфиги старых версий автоматически обновляются до актуальной структуры с комментариями. Старый `vchat-tab.json` сохраняется как резервная копия.

## Placeholders

В `globalFormat`, `localFormat` и `playerFormat` доступны:

| Placeholder | Значение |
|---|---|
| `<prefix>` | Активный prefix LuckPerms с цветами |
| `<suffix>` | Активный suffix LuckPerms с цветами |
| `<name>` | Настоящий ник игрока |
| `<display_name>` | Текущее отображаемое имя игрока |
| `<group>` | Primary group LuckPerms |
| `<world>` | Идентификатор текущего мира |
| `<channel>` | `global`, `local` или `tab` |
| `<message>` | Текст сообщения; используется в форматах чата |

В `header` и `footer` TAB доступны:

| Placeholder | Значение |
|---|---|
| `%online%` | Игроков онлайн |
| `%max%` | Максимальное число игроков |
| `%player%` | Ник игрока, которому отправляется TAB |

## Цвета и permissions

Конфиг, префиксы и разрешённые сообщения игроков поддерживают:

- `&6` — обычный цвет;
- `&l`, `&m`, `&n`, `&o`, `&r` — стили;
- `&k` — obfuscated;
- `#RRGGBB`, `&#RRGGBB`, `&%23RRGGBB` — HEX;
- прописные варианты вроде `&A` и `&#FFAA00`.

Если соответствующий параметр `ForEveryone` равен `false`, право выдаётся через LuckPerms:

| Permission | Возможность |
|---|---|
| `vchat.format.color` | Цвета `&0`–`&f` |
| `vchat.format.hex` | HEX-цвета |
| `vchat.format.style` | Bold, italic, underline, strikethrough и reset |
| `vchat.format.obfuscated` | Эффект `&k` |

Операторы с уровнем прав 2 всегда могут использовать всё оформление.

## Сортировка LuckPerms

Сортировка только одна: VChat получает weight основной группы игрока. При `higherWeightFirst: true` больший weight располагается выше. Prefix отвечает за внешний вид и не влияет на порядок.

| Группа | Weight | Позиция |
|---|---:|---|
| owner | 1000 | Выше всех |
| admin | 900 | Ниже owner |
| moderator | 500 | Ниже admin |
| default | 0 | Внизу |

Игрок без weight располагается внизу. При одинаковом порядке используется вторичный ключ из ника.

## Установка

1. Поместить `VChat-1.3.0.jar` в `mods/` сервера Forge 1.20.1.
2. Установить LuckPerms, если нужны префиксы, permissions и сортировка.
3. Перезапустить сервер.
4. Настроить `config/vchat-config.json5` и выполнить `/vchat reload`.
