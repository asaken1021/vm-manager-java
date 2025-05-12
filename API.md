# APIの簡易リファレンス
- `/vms`
  - GET: 仮想マシンの名前とUUIDを含むJSONのリストを返す
  - POST: JSONを渡すとJSONの内容で仮想マシンを作成
- `/vms/[UUID]`
  - GET: 渡されたUUIDのVMのJSONを返す
  - PUT: 渡されたUUIDのVMの設定を渡されたJSONの内容で更新する
  - DELETE: 渡されたUUIDのVMを削除する
- `/vms/[UUID]/vnc`
  - GET: 渡されたUUIDのVMのVNCにアクセスできるIPアドレスとポート番号を返す
- `/vms/[UUID]/state`
  - GET: 渡されたUUIDのVMの電源状態を返す
  - PUT: 渡されたUUIDのVMの電源状態を変更する
- `/isoimages`
  - GET: `—-iso-images-path`オプションで指定したディレクトリ内のファイルを絶対パスで返す
- `/isoimages/folders`
  - GET: `—-iso-images-path`オプションで指定したディレクトリ内のディレクトリのみを絶対パスで返す
- `/isoimages/files/[Path]`
  - GET: `—-iso-images-path`オプションで指定したディレクトリ内で、Pathに指定した絶対パスのディレクトリ内のファイルのみを絶対パスで返す

# JSON形式のVM情報
CPU1コア、RAM1GB、電源オフの状態の仮想マシンを表現するJSONは以下のようになります。
```json
{
    "vm": {
        "uuid": "d824f3e5-fd90-412e-8fc3-feedcf69d0e9",
        "name": "vm-name",
        "state": "Shut off",
        "cpus": 1,
        "ram": 1024,
        "ram_unit": "MiB",
        "disks": [
            {
                "file_path": "/path/to/vm-disks/disk.qcow2",
                "device": "disk",
                "target_dev": "vda",
                "target_bus": "virtio"
            },
            {
                "file_path": "/path/to/iso-images/some-os-installer.iso",
                "device": "cdrom",
                "target_dev": "sda",
                "target_bus": "sata"
            }
        ],
        "interfaces": [
            {
                "mac_address": "52:54:00:26:12:87",
                "type": "bridge",
                "source": "bridge0",
                "model": "virtio",
                "addresses": []
            }
        ],
        "boot_order": [
            {
                "dev": "hd"
            },
            {
                "dev": "cdrom"
            }
        ]
    }
}
```