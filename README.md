<div align="right">

[![Build Status](https://github.com/YanKarpov/MIA/actions/workflows/build.yml/badge.svg)](https://github.com/YanKarpov/MIA/actions/workflows/build.yml)

</div>

<div align="center">

# Project MIA

## **Тема дипломной работы** 
Проектирование, разработка и тестирование информационной системы генерации динамических квестов и игровых событий в среде Minecraft с применением машинного обучения.

</div>

# Сборка и развертывание проекта

## Сборка плагина

### Основная команда:
```bash
gradle build
```
Данная команда выполняет:
- компиляцию исходного кода
- сборку JAR-файла плагина
- формирование итогового артефакта (fatJar)

### Вторичная команда:
```bash
gradle deploy
```
Данная команда выполняет:
- Очистка старых версий плагина в рабочей директории
- Копирование новой версии плагина
- Перезапуск сервера Minecraft через Docker

# Архитектура
Проект включает следующие основные компоненты:

- QuestService про логику создания и управления квестами
- DBConnector про работу с PostgreSQL базой данных
- QuestListener про обработка игровых событий (блоки, мобы, предметы, смерть игрока)
- QuestGenerator про генерация квестов
- QuestPlugin основной класс плагина (логично, да?)

![Kinger](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNGgxZGV2eHUwaWJsYXlodmhvYzNuMDZoaWVxOGp0MnM4eXpreXNjeCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/TXXjtuMCUihRQj9Z15/giphy.gif)
