package akhoi.libs.mlct.tools.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;

public class FileWatcher {
    private final akhoi.libs.mlct.tools.FileWatcher delegate;

    public FileWatcher(File rootDir) throws IOException {
        this(rootDir, FileSystems.getDefault().newWatchService());
    }

    public FileWatcher(File rootDir, WatchService watchService) {
        this.delegate = new akhoi.libs.mlct.tools.FileWatcher(rootDir, watchService);
    }

    akhoi.libs.mlct.tools.FileWatcher getDelegate() {
        return delegate;
    }

    public Flow<EventData> watchFile(String key) {
        Flow<akhoi.libs.mlct.tools.FileWatcher.EventData> flow = delegate.watchFile(key);
        return FlowKt.map(flow, new Function2<akhoi.libs.mlct.tools.FileWatcher.EventData, Continuation<? super EventData>, Object>() {
            @Override
            public Object invoke(akhoi.libs.mlct.tools.FileWatcher.EventData eventData, Continuation<? super EventData> continuation) {
                return new EventData(eventData);
            }
        });
    }

    public void unwatchFile(String key) {
        delegate.unwatchFile(key);
    }

    public boolean registerDirectory(String key) {
        return delegate.registerDirectory(key);
    }

    public void unregisterDirectory(String key) {
        delegate.unregisterDirectory(key);
    }

    public Object start(Continuation<? super Unit> continuation) {
        return delegate.start(continuation);
    }

    public void stop() {
        delegate.stop();
    }

    public enum EventType {
        CREATED,
        DELETED,
        UPDATED
    }

    public static final class EventData {
        private final akhoi.libs.mlct.tools.FileWatcher.EventData delegate;

        public EventData(akhoi.libs.mlct.tools.FileWatcher.EventData delegate) {
            this.delegate = delegate;
        }

        public EventType getType() {
            akhoi.libs.mlct.tools.FileWatcher.EventType type = delegate.getType();
            switch (type) {
                case CREATED:
                    return EventType.CREATED;
                case DELETED:
                    return EventType.DELETED;
                case UPDATED:
                default:
                    return EventType.UPDATED;
            }
        }

        public java.nio.file.Path getPath() {
            return delegate.getPath();
        }

        akhoi.libs.mlct.tools.FileWatcher.EventData getDelegate() {
            return delegate;
        }
    }
}
