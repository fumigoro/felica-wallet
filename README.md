# Felicaのデータを読み取って蓄積→利用状況を確認できるアプリ
## Overview
対応予定カード
 - Ayuca
 - 大学生協キャンパスペイ
カードから読み込む内容
 - 残高
 - 利用履歴(日時と金額)
 - 利用内容(Ayucaのみ,乗車降車停留所)

## 出典

[Qiita - AndroidでFelica(NFC)のブロックデータの取得](https://qiita.com/nshiba/items/38f94d61c020a17314b6)

この記事内の AndroidでFelicaの読み込み という部分に掲載されているコードをMainActivity.javaに一旦コピペして使用しています。（onNewIntent()メソッド内に一部自分で追加した部分を除く）
NFCReader.javaとReadWithoutEncryption.javaは全て自分で書いたものです。同じ記事内のそれ以外のセクション（サービス部分のデータ取得 など）に掲載のコードは使用していません。

[http://iccard.jennychan.at-ninja.jp/format/ayuca.xml](http://iccard.jennychan.at-ninja.jp/format/ayuca.xml)

Ayucaのバイナリデータの解析ため今後参考にする予定です。

