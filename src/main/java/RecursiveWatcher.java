
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/*
参考サイト
* https://blog1.mammb.com/entry/20120519/1337449611
* */
public class RecursiveWatcher {

    private final WatchService watcher = FileSystems.getDefault().newWatchService();
    private final Map<WatchKey, Path> watchKeys = new HashMap<>();
    private List<Callback> callbacks = new ArrayList<>();

    interface Callback { void onCall(WatchEvent<Path> event, Path path);}


    public RecursiveWatcher(Path dir, Callback... callbacks) throws IOException, IOException {
        this.callbacks.addAll(Arrays.asList(callbacks));
        registerRecursive(dir);
    }

    private void registerRecursive(final Path base) { try {
        Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    } catch(IOException e) { throw new RuntimeException(e);} }


    private void register(final Path dir) { try {
        if (dir == null) return;
        WatchKey key = dir.register(this.watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        this.watchKeys.put(key, dir);
    } catch(IOException e) { throw new RuntimeException(e);} }


    public void start() { try {
        while (true) {
            WatchKey key;
            try {
                key = this.watcher.take(); // wait
            } catch (InterruptedException e) {
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                if (event.kind() == OVERFLOW) continue;

                WatchEvent<Path> watchEvent = cast(event);
                Path path = this.watchKeys.get(key).resolve(watchEvent.context());

                if (Files.isDirectory(path, NOFOLLOW_LINKS)) {
                    if (event.kind() == ENTRY_CREATE) {
                        register(path);
                    }
                } else if (Files.isRegularFile(path, NOFOLLOW_LINKS)) {
                    if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
                        for (Callback cb : this.callbacks) {cb.onCall(watchEvent, path);}
                    }
                }
            }

            if (!key.reset()) {
                this.watchKeys.remove(key);
            }
            for (Iterator<Map.Entry<WatchKey, Path>> it = this.watchKeys.entrySet().iterator(); it.hasNext();) {
                // sweep
                Map.Entry<WatchKey, Path> entry = it.next();
                if(Files.notExists(entry.getValue(), NOFOLLOW_LINKS)) {
                    entry.getKey().cancel();
                    it.remove();
                }
            }
            if (this.watchKeys.isEmpty()) break;
        }} finally { try {
        watcher.close();
    } catch (IOException e) {} }
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
}
