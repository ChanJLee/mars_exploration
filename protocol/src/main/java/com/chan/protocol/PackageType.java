package com.chan.protocol;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by chan on 2018/1/9.
 */

public interface PackageType {
	short TYPE_IMAGE = 1;

	short TYPE_WINDOW_SIZE = 2;

	short TYPE_HEART_BEAT = 3;

	short TYPE_ACTION = 4;

	@IntDef({TYPE_ACTION, TYPE_HEART_BEAT, TYPE_WINDOW_SIZE, TYPE_IMAGE})
	@Retention(RetentionPolicy.SOURCE)
	@interface Type {

	}
}
