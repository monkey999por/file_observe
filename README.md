### Build
・実行可能jarを生成する  
1.intellj ideaを開く  
2.ツールバー > build > build artifacts... > file_observe:jarを選択しbuild

※ビルドの設定はFile > Project structer > artifacts から見られる

### 実行方法
1. intelljの場合  
 Run > Run Main ※引数にsetting.propertiesを指定
   
2. 実行可能jarの場合  
  `start "auto_deploy" /WAIT /B java -Dfile.encoding="utf-8" -jar file_observe.jar deploy.properties`
   
### その他
以下のライブラリを私用してる  
https://github.com/monkey999por/cmd_run  
https://github.com/monkey999por/props
