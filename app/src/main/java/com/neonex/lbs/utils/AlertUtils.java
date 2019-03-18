package com.neonex.lbs.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.neonex.lbs.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by macpro on 2018. 2. 5..
 */

public class AlertUtils {
    public static final int THEME_DEVICE_DEFAULT_DARK = 4;

    public static final int THEME_DEVICE_DEFAULT_LIGHT = 5;

    public static final int THEME_HOLO_DARK = 2;

    public static final int THEME_HOLO_LIGHT = 3;

    public static final int THEME_TRADITIONAL = 1;

    public static final int DEFAULT_THEME = THEME_DEVICE_DEFAULT_LIGHT;

    public static final int HONEY_COMB = 11;

    public static int ID_BUTTON = 0;

    @SuppressWarnings("rawtypes")
    private static final Class[] aMethodName = new Class[] { Context.class,
            int.class };

    private static String alreadyMessage = "";

    private static int alreadyActivity = -1;

    // private static AlertDialog.Builder dialog;

    public static View makeMessageView(Activity activity, String message) {
        View vMessage = View.inflate(activity, R.layout.dialog_alert_message,
                null);
        ((TextView) vMessage.findViewById(R.id.alert_tv_message))
                .setText(message);

        return vMessage;
    }

    public static View makeTitleView(Activity activity, String title) {
        View vTitle = View.inflate(activity, R.layout.dialog_alert_title, null);
        ((TextView) vTitle.findViewById(R.id.title)).setText(title);

        return vTitle;
    }

    public interface OnExcute {
        void onExecute();
    }

    public interface OnExcuteResult {
        void onExcute(int result);
    }

    public interface OnExcuteDialogResult {
        void onExcute(int result, AlertDialog.Builder dialog);
    }

    /**
     * count가 0이면 화면에 Alert를 보여줌</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * @param count
     *            : 비교를 할 count
     * */
    public static void showEmptyListAlert(final Activity activity,
                                          final String message, final int count) {
        if (activity.isFinishing()) {
            return;
        }

        if (count <= 0) {
            showWarning(activity, message);
        }
    }

    /**
     * 화면에 경고 Alert를 보여줌</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * */
    public static void showWarning(final Activity activity, final int message) {
        showWarning(activity, activity.getString(message), null);
    }

    /**
     * 화면에 경고 Alert를 보여줌</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * */
    public static void showWarning(final Activity activity, final String message) {
        showWarning(activity, message, null);
    }

    /**
     * 화면에 경고 Alert를 보여줌</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 경고 타이틀
     * @param message
     *            : 경고 문구
     * */
    public static void showWarning(final Activity activity, final int title,
                                   final int message) {
        showWarning(activity, title, message, null);
    }

    /**
     * 화면에 경고 Alert를 보여줌</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * @param excute
     *            : 확인 버튼 누를 경우 실행될 행동
     * */
    public static void showWarning(final Activity activity, final int message,
                                   final OnExcute excute) {
        showWarning(activity, activity.getString(message), excute);
    }

    /**
     * 화면에 경고 Alert를 보여줌</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * @param excute
     *            : 확인 버튼 누를 경우 실행될 행동
     * */
    public static void showWarning(final Activity activity,
                                   final String message, final OnExcute excute) {
        showWarning(activity, activity.getString(R.string.alert), message,
                excute);
    }

    /**
     * 화면에 경고 Alert를 보여줌</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 타이틀
     * @param message
     *            : 경고 문구
     * @param excute
     *            : 확인 버튼 누를 경우 실행될 행동
     * */
    public static void showWarning(final Activity activity, final int title,
                                   final int message, final OnExcute excute) {
        showWarning(activity, activity.getString(title),
                activity.getString(message), excute);
    }

    /**
     * 화면에 경고 Alert를 보여줌</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 타이틀
     * @param message
     *            : 경고 문구
     * @param excute
     *            : 확인 버튼 누를 경우 실행될 행동
     * */
    public static void showWarning(final Activity activity, final String title,
                                   final String message, final OnExcute excute) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setTitle("");
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excute != null) {
                                    excute.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetDuplicate();

                        if (excute != null) {
                            excute.onExecute();
                        }

                        dialog.dismiss();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    /**
     * 화면에 경고 Alert를 보여줌</BR> Button - 확인 Button - 취소
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 타이틀
     * @param message
     *            : 경고 문구
     * @param excute
     *            : 확인 버튼 누를 경우 실행될 행동
     * @param excuteCancel
     *            : 취소 버튼 누를 경우 실행될 행동
     * */
    public static void showWarning(final Activity activity, final String title,
                                   final String message, final OnExcute excute,
                                   final OnExcute excuteCancel) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setTitle("");
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excute != null) {
                                    excute.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excuteCancel != null) {
                                    excuteCancel.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (excuteCancel != null) {
                            excuteCancel.onExecute();
                        }

                        resetDuplicate();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    public static void showWarning(final Activity activity, final String title,
                                   final String message, final String okBtnTitle,
                                   final String cancleBtnTitle, final OnExcute excute,
                                   final OnExcute excuteCancel) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setTitle("");
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(okBtnTitle,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excute != null) {
                                    excute.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setNegativeButton(cancleBtnTitle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excuteCancel != null) {
                                    excuteCancel.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (excuteCancel != null) {
                            excuteCancel.onExecute();
                        }

                        resetDuplicate();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    public static void showWarning(final Activity activity, final String title,
                                   final String message, final String okBtnTitle,
                                   final String natualBtnTitle ,
                                   final String cancleBtnTitle,
                                   final OnExcute excute,
                                   final OnExcute excuteNatual ,
                                   final OnExcute excuteCancel) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setTitle("");
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(okBtnTitle,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excute != null) {
                                    excute.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setNegativeButton(cancleBtnTitle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excuteCancel != null) {
                                    excuteCancel.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setNeutralButton(natualBtnTitle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excuteNatual != null) {
                                    excuteNatual.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (excuteCancel != null) {
                            excuteCancel.onExecute();
                        }

                        resetDuplicate();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    public static void showWarning(final Activity activity,
                                   final String message, final String okBtnTitle,
                                   final String cancleBtnTitle, final OnExcute excute,
                                   final OnExcute excuteCancel) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);

                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(okBtnTitle,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excute != null) {
                                    excute.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setNegativeButton(cancleBtnTitle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excuteCancel != null) {
                                    excuteCancel.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (excuteCancel != null) {
                            excuteCancel.onExecute();
                        }

                        resetDuplicate();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    public static void showWarningExit(final Activity activity,
                                       final String title, final String message, final OnExcute excute,
                                       final OnExcute excuteCancel) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setTitle("");
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(R.string.SAVE_EIXT,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excute != null) {
                                    excute.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setNegativeButton(R.string.EXIT, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        resetDuplicate();

                        if (excuteCancel != null) {
                            excuteCancel.onExecute();
                        }
                        arg0.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (excuteCancel != null) {
                            excuteCancel.onExecute();
                        }

                        resetDuplicate();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    /**
     * 확인 버튼을 누르면 해당 activity가 finish되는 Alert</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * */
    public static void showFinishDialog(final Activity activity,
                                        final int message) {
        showFinishDialog(activity, activity.getString(R.string.alert),
                activity.getString(message));
    }

    /**
     * 확인 버튼을 누르면 해당 activity가 finish되는 Alert</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * */
    public static void showFinishDialog(final Activity activity,
                                        final String message) {
        showFinishDialog(activity, activity.getString(R.string.alert), message);
    }

    /**
     * 확인 버튼을 누르면 해당 activity가 finish되는 Alert</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 타이틀
     * @param message
     *            : 경고 문구
     * */
    public static void showFinishDialog(final Activity activity,
                                        final int title, final int message) {
        showFinishDialog(activity, activity.getString(title),
                activity.getString(message));
    }

    /**
     * 확인 버튼을 누르면 해당 activity가 finish되는 Alert</BR> Button - 확인
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 타이틀
     * @param message
     *            : 경고 문구
     * */
    public static void showFinishDialog(final Activity activity,
                                        final String title, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                resetDuplicate();
                                activity.setResult(Activity.RESULT_OK);
                                activity.finish();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetDuplicate();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });
                builder.setCancelable(false);

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    /**
     * 재시도를 확인하는 Alert</BR> 확인 버튼 누를시 현재 Class의 onRetry method의 내용을 실행, 없을시 확인
     * 버튼 누르면 아무 동작 없음</BR> Button - 확인, 취소
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * @param clazz
     *            : 현재 Class 명
     * */
    public static void showRetry(final Activity activity, final int message,
                                 final Class<?> clazz) {
        showRetry(activity, activity.getString(R.string.alert),
                activity.getString(message), clazz);
    }

    /**
     * 재시도를 확인하는 Alert</BR> 확인 버튼 누를시 현재 Class의 onRetry method의 내용을 실행, 없을시 확인
     * 버튼 누르면 아무 동작 없음</BR> Button - 확인, 취소
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * @param clazz
     *            : 현재 Class 명
     * */
    public static void showRetry(final Activity activity, final String message,
                                 final Class<?> clazz) {
        showRetry(activity, activity.getString(R.string.alert), message, clazz);
    }

    /**
     * 재시도를 확인하는 Alert - 확인 버튼 누를시 현재 Class의 onRetry method의 내용을 실행, 없을시 확인 버튼
     * 누르면 아무 동작 없음</BR> Button - 확인, 취소
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * @param clazz
     *            : 현재 Class 명
     * */
    public static void showRetry(final Activity activity, final String title,
                                 final String message, final Class<?> clazz) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(R.string.retry,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                resetDuplicate();

                                try {
                                    clazz.cast(activity)
                                            .getClass()
                                            .getMethod("onRetry",
                                                    (Class[]) null)
                                            .invoke(clazz.cast(activity),
                                                    new Object[0]);
                                } catch (Exception e) {
                                }
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetDuplicate();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                resetDuplicate();

                                dialog.dismiss();
                                activity.finish();
                            }
                        });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });
                builder.setCancelable(false);

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    /**
     * 선택가능 Dialog Button - 확인, 취소
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * @param excute
     *            : Ok button 실행
     * @param cancelExcute
     *            : Cancel button 실행
     * */
    public static void showSelectDialog(final Activity activity,
                                        final String message, final OnExcute excute,
                                        final OnExcute cancelExcute) {
        showSelectDialog(activity, activity.getString(R.string.alert), message,
                excute, cancelExcute);
    }

    /**
     * 선택가능 Dialog Button - 확인, 취소
     *
     * @param activity
     *            : parent activity
     * @param message
     *            : 경고 문구
     * @param excute
     *            : Ok button 실행
     * @param cancelExcute
     *            : Cancel button 실행
     * */
    public static void showSelectDialog(final Activity activity,
                                        final int message, final OnExcute excute,
                                        final OnExcute cancelExcute) {
        showSelectDialog(activity, activity.getString(R.string.alert),
                activity.getString(message), excute, cancelExcute);
    }

    /**
     * 선택가능 Dialog Button - 확인, 취소
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 타이틀
     * @param message
     *            : 경고 문구
     * @param excute
     *            : Ok button 실행
     * @param cancelExcute
     *            : Cancel button 실행
     * */
    public static void showSelectDialog(final Activity activity,
                                        final int title, final int message, final OnExcute excute,
                                        final OnExcute cancelExcute) {
        showSelectDialog(activity, activity.getString(title),
                activity.getString(message), excute, cancelExcute);
    }

    /**
     * 선택가능 Dialog Button - 확인, 취소
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 타이틀
     * @param message
     *            : 경고 문구
     * @param excute
     *            : Ok button 실행
     * @param cancelExcute
     *            : Cancel button 실행
     * */
    public static void showSelectDialog(final Activity activity,
                                        final String title, final String message, final OnExcute excute,
                                        final OnExcute cancelExcute) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excute != null) {
                                    excute.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (cancelExcute != null) {
                                    cancelExcute.onExecute();
                                }

                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetDuplicate();

                        if (cancelExcute != null) {
                            cancelExcute.onExecute();
                        }

                        dialog.dismiss();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    /**
     * 선택가능 Dialog Button - 확인, 취소
     *
     * @param activity
     *            : parent activity
     * @param title
     *            : 타이틀
     * @param message
     *            : 경고 문구
     * @param excute
     *            : Ok button 실행
     * @param cancelExcute
     *            : Cancel button 실행
     * */
    public static void showSelectDialog(final Activity activity,
                                        final String title, final String message,
                                        final String positiveButtonMsg, final String negativeButtonMsg,
                                        final OnExcute excute, final OnExcute cancelExcute) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                if (duplicateCheck(activityHash, message)) {
                    return;
                }

                alreadyActivity = activityHash;
                alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                builder.setView(makeMessageView(activity, message));
                builder.setPositiveButton(positiveButtonMsg,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (excute != null) {
                                    excute.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setNegativeButton(negativeButtonMsg,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                resetDuplicate();

                                if (cancelExcute != null) {
                                    cancelExcute.onExecute();
                                }

                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetDuplicate();

                        if (cancelExcute != null) {
                            cancelExcute.onExecute();
                        }

                        dialog.dismiss();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    public static void showChoiceDialog(final Activity activity,
                                        final int title, final int arrayResId, final OnExcuteResult excute) {
        showChoiceDialog(activity, activity.getString(title), activity
                .getResources().getStringArray(arrayResId), excute);
    }

    public static void showChoiceDialog(final Activity activity,
                                        final String title, final CharSequence[] choiceList,
                                        final OnExcuteResult excute) {
        showChoiceDialog(activity, title, (Object) choiceList, excute);
    }

    public static void showChoiceDialog(final Activity activity,
                                        final String title, final Object object, final OnExcuteResult excute) {
        showChoiceDialog(activity, title, object, -1, excute);
    }

    public static void showChoiceDialog(final Activity activity,
                                        final String title, final Object object, final int select,
                                        final OnExcuteResult excute) {
        final int activityHash = activity.hashCode();
        if (duplicateCheck(activityHash, title)) {
            return;
        }

        alreadyActivity = activityHash;
        alreadyMessage = title;

        AlertDialog.Builder builder = makeBuilder(activity);
        builder.setCustomTitle(makeTitleView(activity, title));

        if (object instanceof ListAdapter) {
            builder.setSingleChoiceItems((ListAdapter) object, select,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetDuplicate();
                            if (excute != null) {
                                excute.onExcute(which);
                            }
                            dialog.dismiss();
                        }
                    });
        } else if (object instanceof CharSequence[]) {
            builder.setSingleChoiceItems((CharSequence[]) object, select,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetDuplicate();
                            if (excute != null) {
                                excute.onExcute(which);
                            }
                            dialog.dismiss();
                        }
                    });
        } else if (object instanceof String[]) {
            builder.setSingleChoiceItems((String[]) object, select,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetDuplicate();
                            if (excute != null) {
                                excute.onExcute(which);
                            }
                            dialog.dismiss();
                        }
                    });
        }

        builder.setPositiveButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        resetDuplicate();
                        dialog.dismiss();
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                resetDuplicate();
                dialog.dismiss();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH
                        && event.getRepeatCount() == 0) {
                    return true;
                }
                return false;
            }
        });
        builder.setCancelable(true);
        // builder.create();
        // builder.show();
        AlertDialog tmpAlert = builder.create();
        tmpAlert.setCanceledOnTouchOutside(false);
        tmpAlert.show();
    }

    public static void showImageButtonDialog(final Activity activity,
                                             final String title, final String message, final String[] btns,
                                             final OnExcuteDialogResult excute) {
        final int activityHash = activity.hashCode();
        if (duplicateCheck(activityHash, title)) {
            return;
        }

        alreadyActivity = activityHash;
        alreadyMessage = title;

        AlertDialog.Builder builder = makeBuilder(activity);
        builder.setCustomTitle(makeTitleView(activity, title));
        View cc = makeCustomView(activity, message, btns, excute);
        builder.setView(cc);
        // dialog = builder;
        builder.setPositiveButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        resetDuplicate();
                        dialog.dismiss();
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                resetDuplicate();
                dialog.dismiss();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH
                        && event.getRepeatCount() == 0) {
                    return true;
                }
                return false;
            }
        });

        builder.setCancelable(true);
        // builder.create();
        // builder.show();
        AlertDialog tmpAlert = builder.create();
        tmpAlert.setCanceledOnTouchOutside(false);
        tmpAlert.show();
    }

    public static LinearLayout makeCustomView(Activity activity, String title,
                                              String[] btnName, OnExcuteDialogResult excute) {
        LinearLayout llCustom = (LinearLayout) View.inflate(activity,
                R.layout.dialog_alert_title, null);
        ((TextView) llCustom.findViewById(R.id.title)).setText(title);
        Button btn = null;
        ID_BUTTON = 0;
        for (String str : btnName) {
            btn = new Button(activity);
            btn.setText(str);
            btn.setId(ID_BUTTON++);
            btn.setTag(excute);
            btn.setOnClickListener(mClickListener);
            llCustom.addView(btn);
        }

        return llCustom;
    }

    public static View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            @SuppressWarnings("unused")
            OnExcuteDialogResult excute = (OnExcuteDialogResult) v.getTag();
            // excute.onExecute(v.getId());
        }
    };

    public static void showToast(final Activity activity, final String message,
                                 final int duration) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, duration).show();
            }
        });
    }

    public static void showToast(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showToast(final Context context, final Handler handler,
                                 final String message, final int duration) {
        if (handler == null) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), message,
                        duration).show();
            }
        });
    }

    public static void showToast(final Context context, final Handler handler,
                                 final String message) {
        if (handler == null) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static boolean duplicateCheck(int activityHash, String message) {
        if (alreadyActivity == activityHash && alreadyMessage.equals(message)) {
            return true;
        } else {
            return false;
        }
    }

    private static void resetDuplicate() {
        alreadyMessage = "";
        alreadyActivity = -1;
    }

    @SuppressWarnings("rawtypes")
    public static AlertDialog.Builder makeBuilder(Activity activity) {
        Constructor constructor = getConstructor(AlertDialog.Builder.class,
                aMethodName);
        if (constructor != null) {
            return (AlertDialog.Builder) getInstance(constructor, activity,
                    DEFAULT_THEME);
        } else {
            return new AlertDialog.Builder(activity);
        }
    }

    public static ProgressDialog makeProgressDialog(Activity activity) {
        return new ProgressDialog(activity);
    }

    // public static FullLayoutProgressDialog
    // makeFullLayoutProgressDialog(Activity activity)
    // {
    // return new FullLayoutProgressDialog(activity);
    // }
    //
    // public static StyleChangeProgressDialog
    // makeStyleChangeProgressDialog(Activity activity)
    // {
    // if(PhoneUtil.getSDKVersion() >= HONEY_COMB)
    // {
    // return new StyleChangeProgressDialog(activity, DEFAULT_THEME);
    // }
    // else
    // {
    // return new StyleChangeProgressDialog(activity);
    // }
    // }
    //
    // public static ButtonMenuDialog makeCustomDialog(Activity activity)
    // {
    // if(PhoneUtil.getSDKVersion() >= HONEY_COMB)
    // {
    // return new ButtonMenuDialog(activity, DEFAULT_THEME);
    // }
    // else
    // {
    // return new ButtonMenuDialog(activity);
    // }
    // }

    public static void showCustomViewNotitle(final Activity activity,final int layoutResId,
                                             final onMakeView makeView, final OnExcute excute,
                                             final String excuteTitle) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                // if (duplicateCheck(activityHash, message)) {
                // return;
                // }

                alreadyActivity = activityHash;
                // alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                View view = makeCustomView(activity, layoutResId);
                builder.setView(view);
                makeView.getView(view);

                builder.setPositiveButton(excuteTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        resetDuplicate();

                        if (excute != null) {
                            excute.onExecute();
                        }
                        arg0.dismiss();
                    }
                });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetDuplicate();

                        if (excute != null) {
                            excute.onExecute();
                        }

                        dialog.dismiss();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    tmpAlert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    tmpAlert.show();
                }
            }
        });
    }

    public interface onMakeView {
        public void getView(View v);
    }

    public static View makeCustomView(Activity activity, int layoutResId) {

        View vMessage = View.inflate(activity, layoutResId, null);
        return vMessage;
    }


    public static void showCustomView(final Activity activity,
                                      final String title, final int layoutResId,
                                      final onMakeView makeView, final OnExcute excute,
                                      final String excuteTitle, final OnExcute excuteCancel,
                                      final String excuteCancelTitle) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                // if (duplicateCheck(activityHash, message)) {
                // return;
                // }

                alreadyActivity = activityHash;
                // alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setTitle("");
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                View view = makeCustomView(activity, layoutResId);
                builder.setView(view);
                makeView.getView(view);

                builder.setPositiveButton(excuteTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        resetDuplicate();

                        if (excute != null) {
                            excute.onExecute();
                        }
                        arg0.dismiss();
                    }
                });
                builder.setNegativeButton(excuteCancelTitle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int which) {
                                resetDuplicate();

                                if (excuteCancel != null) {
                                    excuteCancel.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetDuplicate();

                        if (excute != null) {
                            excute.onExecute();
                        }

                        dialog.dismiss();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    public static void showCustomView(final Activity activity,
                                      final String title, final View customView,
                                      final onMakeView makeView, final OnExcute excute,
                                      final String excuteTitle, final OnExcute excuteCancel,
                                      final String excuteCancelTitle) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                // if (duplicateCheck(activityHash, message)) {
                // return;
                // }

                alreadyActivity = activityHash;
                // alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);
                if (title != null) {
                    builder.setTitle("");
                    builder.setCustomTitle(makeTitleView(activity, title));
                }
                View view = customView ;
                builder.setView(view);
                makeView.getView(view);

                builder.setPositiveButton(excuteTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        resetDuplicate();

                        if (excute != null) {
                            excute.onExecute();
                        }
                        arg0.dismiss();
                    }
                });
                builder.setNegativeButton(excuteCancelTitle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int which) {
                                resetDuplicate();

                                if (excuteCancel != null) {
                                    excuteCancel.onExecute();
                                }
                                arg0.dismiss();
                            }
                        });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        resetDuplicate();

                        if (excute != null) {
                            excute.onExecute();
                        }

                        dialog.dismiss();
                    }
                });
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }

    public static void showCustomViewSimple(final Activity activity, final View customView,
                                            final String excuteTitle , final OnExcute excute) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int activityHash = activity.hashCode();
                // if (duplicateCheck(activityHash, message)) {
                // return;
                // }

                alreadyActivity = activityHash;
                // alreadyMessage = message;

                AlertDialog.Builder builder = makeBuilder(activity);

                View view = customView ;
                builder.setView(view);

                builder.setPositiveButton(excuteTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        resetDuplicate();

                        if (excute != null) {
                            excute.onExecute();
                        }
                        arg0.dismiss();
                    }
                });

                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH
                                && event.getRepeatCount() == 0) {
                            return true;
                        }
                        return false;
                    }
                });

                if (!activity.isFinishing()) {
                    // builder.show();
                    AlertDialog tmpAlert = builder.create();
                    tmpAlert.setCanceledOnTouchOutside(false);
                    tmpAlert.show();
                }
            }
        });
    }


    @SuppressWarnings("rawtypes")
    public static Constructor getConstructor(Class<AlertDialog.Builder> clazzName,
                                             Class[] methodName) {
        try {
            return clazzName.getConstructor(methodName);
        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public static Object getInstance(Constructor constructor, Object... object) {
        try {
            return constructor.newInstance(object);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }
}