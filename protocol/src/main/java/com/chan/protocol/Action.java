package com.chan.protocol;

import android.support.annotation.IntDef;

/**
 * Created by chan on 2018/1/11.
 */

public interface Action {
	int ACTION_UP = 1;
	int ACTION_DOWN = 2;
	int ACTION_LEFT = 4;
	int ACTION_RIGHT = 8;

	@IntDef({ACTION_UP, ACTION_DOWN, ACTION_LEFT, ACTION_RIGHT})
	@interface ActionType {

	}
}
