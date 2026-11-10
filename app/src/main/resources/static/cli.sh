#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: bash <(curl -fsSL http://SERVER:30080/cli.sh) http://SERVER:30080" >&2
  exit 2
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "Python 3 is required on this client instance." >&2
  exit 2
fi

python3 /dev/fd/3 "$1" 3<<'PY'
import json
import sys
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import quote
from urllib.request import urlopen

base = sys.argv[1].rstrip('/')
if not base.startswith(('http://', 'https://')):
    sys.exit('Server URL must start with http:// or https://')

try:
    with urlopen(base + '/api/files', timeout=15) as response:
        files = json.load(response)
except HTTPError as error:
    if error.code == 403:
        sys.exit('No access: ask the admin to add this instance\'s public IP to the allowed list.')
    sys.exit(f'Could not list files: HTTP {error.code}')
except (URLError, TimeoutError) as error:
    sys.exit(f'Could not reach VaultShare: {error}')

if not files:
    sys.exit('No files are available to download.')

print('\nVaultShare files:')
for number, item in enumerate(files, 1):
    print(f'{number}. {item["filename"]} ({item["sizeBytes"]} bytes)')

try:
    answer = input('\nEnter a file number to download (or q to quit): ').strip()
except EOFError:
    sys.exit('\nNo selection made.')
if answer.lower() == 'q':
    sys.exit(0)
if not answer.isdecimal() or not 1 <= int(answer) <= len(files):
    sys.exit('Invalid file number.')

selected = files[int(answer) - 1]
filename = Path(selected['filename']).name or 'download'
destination = Path.cwd() / filename
if destination.exists():
    stem = destination.stem
    suffix = destination.suffix
    for counter in range(1, 1000):
        candidate = destination.with_name(f'{stem}-{counter}{suffix}')
        if not candidate.exists():
            destination = candidate
            break
    else:
        sys.exit('Could not choose a new filename; too many existing copies.')

try:
    with urlopen(base + '/api/files/' + quote(selected['id']), timeout=60) as response:
        with destination.open('xb') as output:
            while chunk := response.read(1024 * 1024):
                output.write(chunk)
except HTTPError as error:
    if error.code == 403:
        sys.exit('No access: this instance is not allowed to download files.')
    sys.exit(f'Download failed: HTTP {error.code}')
except (URLError, TimeoutError) as error:
    sys.exit(f'Download failed: {error}')

print(f'Downloaded: {destination}')
PY
