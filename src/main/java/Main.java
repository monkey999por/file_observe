
import monkey999.tools.Cmd;
import monkey999.tools.Setting;

import java.io.IOException;
import java.nio.file.*;

public class Main {


    /**
     * @param args 0: properties
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Setting.init(args[0]);
        new RecursiveWatcher(Paths.get(Setting.getAsString("watch_root")), new RecursiveWatcher.Callback(){
            public void onCall(WatchEvent<Path> event, Path path) {
                System.out.format("onCall: %s : %s\n", event.kind().name(), path);
                try {
                    Cmd.execute(false, new String[]{"cmd","/C", Setting.getAsString("batch")});
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
        System.out.println("監視を開始しました。");
        Thread.sleep(3600);

    }

}
