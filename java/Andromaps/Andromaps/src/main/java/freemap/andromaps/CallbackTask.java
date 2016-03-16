package freemap.andromaps;

/**
 * Created by nick on 16/03/16.
 */

import android.content.Context;

public abstract class CallbackTask<Params,Progress> extends ConfigChangeSafeTask<Params, Progress>{

    public CallbackTask(Context ctx)
    {
        super(ctx);
    }

    public abstract void reconnect(Context ctx, Object callback);
}
