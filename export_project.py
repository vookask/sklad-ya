#!/usr/bin/env python3
"""
Скрипт для экспорта проекта в единый Markdown-файл.
Сканирует директорию, создает дерево проекта и включает содержимое всех текстовых файлов.
"""

import os
import sys
from pathlib import Path
from typing import Set, Tuple, Optional

# Устанавливаем UTF-8 кодировку для вывода в консоль Windows
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

# Константы для игнорирования
IGNORED_DIRS = {
    'node_modules', '.git', '__pycache__', '.venv', 'venv',
    'dist', '.idea', '.vscode'
}

IGNORED_FILES = {
    'package-lock.json', 'yarn.lock', '.env', 'project_dump.md',
    'export_project.py'
}

IGNORED_EXTENSIONS = {
    # Логи и кэш
    '.log', '.pyc', '.cache', '.DS_Store',
    # Медиа-файлы
    '.png', '.jpg', '.jpeg', '.gif', '.svg', '.webp', '.mp4', '.mov', '.ico', '.avif',
    # Шрифты
    '.woff', '.woff2', '.ttf', '.eot',
    # Архивы
    '.zip', '.gz', '.tar'
}

MAX_FILE_SIZE = 1024 * 1024  # 1 МБ

# Соответствие расширений файлов языкам для подсветки синтаксиса в Markdown
LANGUAGE_MAP = {
    '.py': 'python',
    '.js': 'javascript',
    '.ts': 'typescript',
    '.tsx': 'typescript',
    '.jsx': 'javascript',
    '.java': 'java',
    '.kt': 'kotlin',
    '.kts': 'kotlin',
    '.xml': 'xml',
    '.json': 'json',
    '.yaml': 'yaml',
    '.yml': 'yaml',
    '.toml': 'toml',
    '.properties': 'properties',
    '.gradle': 'gradle',
    '.md': 'markdown',
    '.txt': 'text',
    '.sh': 'bash',
    '.bat': 'batch',
    '.cmd': 'batch',
    '.html': 'html',
    '.css': 'css',
    '.scss': 'scss',
    '.less': 'less',
    '.sql': 'sql',
    '.rb': 'ruby',
    '.go': 'go',
    '.rs': 'rust',
    '.cpp': 'cpp',
    '.c': 'c',
    '.h': 'c',
    '.hpp': 'cpp',
    '.cs': 'csharp',
    '.php': 'php',
    '.swift': 'swift',
    '.dart': 'dart',
    '.groovy': 'groovy',
    '.pro': 'prolog',
    '.gradle.kts': 'kotlin',
}


def should_ignore_dir(dir_name: str) -> bool:
    """Проверяет, нужно ли игнорировать директорию по названию."""
    if dir_name in IGNORED_DIRS:
        return True
    if dir_name.endswith('.egg-info'):
        return True
    return False


def should_ignore_file(file_path: Path) -> Tuple[bool, Optional[str]]:
    """
    Проверяет, нужно ли игнорировать файл.
    Возвращает кортеж (игнорировать, причина).
    """
    file_name = file_path.name
    
    # Проверка по точному имени
    if file_name in IGNORED_FILES:
        return True, f"игнорируемый файл: {file_name}"
    
    # Проверка по расширению
    ext = file_path.suffix.lower()
    if ext in IGNORED_EXTENSIONS:
        return True, f"игнорируемое расширение: {ext}"
    
    # Проверка по размеру
    try:
        file_size = file_path.stat().st_size
        if file_size > MAX_FILE_SIZE:
            return True, f"размер превышает 1 МБ: {file_size / 1024 / 1024:.2f} МБ"
    except OSError:
        return True, "ошибка при получении размера файла"
    
    return False, None


def get_language_for_file(file_path: Path) -> str:
    """Возвращает язык для подсветки синтаксиса в Markdown."""
    ext = file_path.suffix.lower()
    
    # Сначала проверяем точное совпадение
    if ext in LANGUAGE_MAP:
        return LANGUAGE_MAP[ext]
    
    # Проверяем составные расширения (например, .gradle.kts)
    full_name = file_path.name.lower()
    for pattern, lang in LANGUAGE_MAP.items():
        if full_name.endswith(pattern):
            return lang
    
    return 'text'


def is_text_file(file_path: Path) -> Tuple[bool, Optional[str]]:
    """
    Проверяет, является ли файл текстовым.
    Возвращает кортеж (является_текстовым, причина_отказа).
    """
    try:
        with open(file_path, 'rb') as f:
            # Читаем первые 8192 байт для проверки
            chunk = f.read(8192)
            if not chunk:
                return True, None  # Пустой файл считаем текстовым
            
            # Пытаемся декодировать как UTF-8
            chunk.decode('utf-8')
            return True, None
    except UnicodeDecodeError:
        return False, "файл не является текстовым (не UTF-8)"
    except Exception as e:
        return False, f"ошибка при чтении файла: {e}"


def generate_tree_structure(root_path: Path) -> str:
    """Генерирует дерево проекта в формате Markdown."""
    lines = []
    lines.append("# 📁 Структура проекта\n")
    lines.append("```\n")
    
    def build_tree(path: Path, prefix: str = "", is_last: bool = True):
        try:
            items = sorted(path.iterdir(), key=lambda x: (x.is_file(), x.name.lower()))
        except PermissionError:
            return
        
        # Фильтруем игнорируемые директории и файлы
        items = [
            item for item in items
            if not (item.is_dir() and should_ignore_dir(item.name))
            and not (item.is_file() and should_ignore_file(item)[0])
        ]
        
        for i, item in enumerate(items):
            is_last_item = i == len(items) - 1
            
            # Определяем префиксы для текущего элемента
            connector = "└── " if is_last_item else "├── "
            lines.append(f"{prefix}{connector}{item.name}\n")
            
            # Рекурсивно обрабатываем директории
            if item.is_dir():
                extension = "    " if is_last_item else "│   "
                build_tree(item, prefix + extension, is_last_item)
    
    build_tree(root_path)
    lines.append("```\n")
    return "".join(lines)


def read_file_content(file_path: Path) -> Optional[str]:
    """Читает содержимое файла как текст."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            return f.read()
    except Exception as e:
        return None


def export_project(root_path: Path, output_file: str = "project_dump.md"):
    """
    Основная функция экспорта проекта.
    
    Args:
        root_path: Корневая директория проекта
        output_file: Имя выходного файла
    """
    stats = {
        'included': 0,
        'skipped': 0,
        'skip_reasons': {}
    }
    
    print(f"🚀 Начинаем экспорт проекта из: {root_path}")
    print(f"📝 Выходной файл: {output_file}\n")
    
    # Собираем все файлы для обработки
    files_to_process = []
    
    def collect_files(path: Path):
        try:
            for item in path.iterdir():
                if item.is_dir():
                    if should_ignore_dir(item.name):
                        print(f"  ⏭️  Пропущена директория: {item.name}")
                        continue
                    collect_files(item)
                elif item.is_file():
                    should_skip, reason = should_ignore_file(item)
                    if should_skip:
                        stats['skipped'] += 1
                        stats['skip_reasons'][reason] = stats['skip_reasons'].get(reason, 0) + 1
                        print(f"  ⏭️  Пропущен файл: {item} ({reason})")
                        continue
                    
                    is_text, text_reason = is_text_file(item)
                    if not is_text:
                        stats['skipped'] += 1
                        stats['skip_reasons'][text_reason] = stats['skip_reasons'].get(text_reason, 0) + 1
                        print(f"  ⏭️  Пропущен файл: {item} ({text_reason})")
                        continue
                    
                    files_to_process.append(item)
        except PermissionError:
            print(f"  ⚠️  Нет доступа к директории: {path}")
    
    collect_files(root_path)
    
    print(f"\n📊 Найдено файлов для обработки: {len(files_to_process)}\n")
    
    # Генерируем содержимое Markdown
    markdown_content = []
    
    # Заголовок
    markdown_content.append("# 📦 Экспорт проекта\n\n")
    markdown_content.append(f"**Корневая директория:** `{root_path}`\n\n")
    markdown_content.append("---\n\n")
    
    # Дерево проекта
    markdown_content.append(generate_tree_structure(root_path))
    markdown_content.append("---\n\n")
    markdown_content.append("# 📄 Содержимое файлов\n\n")
    
    # Обрабатываем файлы
    for i, file_path in enumerate(files_to_process, 1):
        relative_path = file_path.relative_to(root_path)
        language = get_language_for_file(file_path)
        
        print(f"  [{i}/{len(files_to_process)}] 📄 Обработка: {relative_path}")
        
        content = read_file_content(file_path)
        if content is None:
            print(f"    ⚠️  Ошибка при чтении файла")
            stats['skipped'] += 1
            stats['skip_reasons']['ошибка при чтении'] = stats['skip_reasons'].get('ошибка при чтении', 0) + 1
            continue
        
        stats['included'] += 1
        
        # Добавляем заголовок файла
        markdown_content.append(f"--- START OF FILE `{relative_path}` ---\n\n")
        markdown_content.append(f"```{language}\n")
        markdown_content.append(content)
        markdown_content.append("\n```\n\n")
    
    # Записываем в файл
    output_path = root_path / output_file
    try:
        with open(output_path, 'w', encoding='utf-8') as f:
            f.writelines(markdown_content)
        print(f"\n✅ Файл успешно создан: {output_path}")
    except Exception as e:
        print(f"\n❌ Ошибка при записи файла: {e}")
        return
    
    # Выводим статистику
    print("\n" + "=" * 50)
    print("📈 Статистика экспорта:")
    print("=" * 50)
    print(f"✅ Включено файлов: {stats['included']}")
    print(f"⏭️  Пропущено файлов: {stats['skipped']}")
    print(f"📊 Всего обработано: {stats['included'] + stats['skipped']}")
    
    if stats['skip_reasons']:
        print("\n📋 Причины пропуска:")
        for reason, count in sorted(stats['skip_reasons'].items(), key=lambda x: x[1], reverse=True):
            print(f"  • {reason}: {count}")
    
    print("=" * 50)


def main():
    """Точка входа в скрипт."""
    # Получаем директорию, из которой запущен скрипт
    script_dir = Path(__file__).parent.resolve()
    
    # Проверяем, что мы находимся в директории проекта
    if not script_dir.exists():
        print(f"❌ Директория не существует: {script_dir}")
        sys.exit(1)
    
    # Запускаем экспорт
    export_project(script_dir)


if __name__ == "__main__":
    main()
