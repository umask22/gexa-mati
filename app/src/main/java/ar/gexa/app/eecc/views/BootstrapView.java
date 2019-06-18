package ar.gexa.app.eecc.views;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.zxing.Result;

import java.io.File;

import ar.gexa.app.eecc.R;
import ar.gexa.app.eecc.models.User;
import ar.gexa.app.eecc.repository.UserRepository;
import ar.gexa.app.eecc.rest.Callback;
import ar.gexa.app.eecc.rest.GexaClient;
import ar.gexa.app.eecc.services.FCMTokenRefreshService;
import ar.gexa.app.eecc.services.UserService;
import ar.gexa.app.eecc.utils.AndroidUtils;
import ar.gexa.app.eecc.widget.AlertWidget;
import common.models.resources.UserResource;
import me.dm7.barcodescanner.core.DisplayUtils;
import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Response;

public class BootstrapView extends AppCompatActivity implements ZXingScannerView.ResultHandler {

//    private RecyclerView recyclerView;
//    private RecyclerViewAdapter recyclerViewAdapter;
    private View loadingView;
    private FrameLayout qrView;

    private TextView titleView;

    private ZXingScannerView qrScanner;
    boolean validateUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrScanner.setResultHandler(this);
        qrScanner.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GexaClient.getInstance().cancel();
        qrScanner.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GexaClient.getInstance().cancel();
    }

    private void bind() {

        setContentView(R.layout.bootstrap);

//        deleteCache(this);
//        recyclerViewAdapter = new RecyclerViewAdapter();
//
//        recyclerView = findViewById(R.id.list);
//        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
//        recyclerView.setAdapter(recyclerViewAdapter);

        qrView = findViewById(R.id.content_frame);
        qrView.setVisibility(View.VISIBLE);

        loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.GONE);

        titleView = findViewById(R.id.titleView);

        qrScanner = new ZXingScannerView(this){
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomZXingScannerView(context);
            }

        };
        qrView.addView(qrScanner);

        if(getIntent().getExtras().getBoolean("isSynchronizationInProgress")) {
            qrView.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
            titleView.setText("¡Hola " + UserRepository.getInstance().find().getName() + "!");
        }
    }

    private void onUserSelected(String code) {

        qrView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);

        GexaClient.getInstance().findUserById(code, new Callback<UserResource>(this) {

            @Override
            public void onSuccess(UserResource user) {
                if(user != null) {
                    validateUser = true;
                    titleView.setText("¡Hola " + user.name + "!");
                    UserService.getInstance().onUserSelected(user);
                    authenticate();
                }
            }

            @Override
            public void onNotFound(Response<UserResource> response) {
                AlertWidget.create().showInfo(BootstrapView.this, "Autenticacion", "El usuario no existe o esta inhabilitado");
                validateUser = false;
            }

            @Override
            public void onError() {
                qrView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    private void authenticate() {

        startService(new Intent(this, FCMTokenRefreshService.class));
        AndroidUtils.loop(new AndroidUtils.LoopListener() {
            @Override
            public boolean loop() {
                final User user = UserRepository.getInstance().find();
                return user != null && user.getDeviceToken() == null;
            }

            @Override
            public void onFinish() {
                GexaClient.getInstance().authenticate(new Callback<User>(BootstrapView.this) {
                    @Override
                    public void onSuccess(User user) {}
                });
            }
        });
    }

    @Override
    public void handleResult(Result result) {
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
        onUserSelected(result.getText());

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(validateUser)
                    qrScanner.stopCamera();
                else
                    qrScanner.resumeCameraPreview(BootstrapView.this);
            }
        }, 2000);
    }

//    private class RecyclerViewAdapter extends RecyclerView.Adapter<UserSearchItemView> {
//
//        private List<UserResource> users = new ArrayList<>();
//
//        public void refresh(final List<UserResource> users) {
//            if (users != null) {
//                this.users.clear();
//                this.users.addAll(users);
//                notifyDataSetChanged();
//            }
//        }
//
//        @Override
//        public UserSearchItemView onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.user_search_item, parent, false);
//            return new UserSearchItemView(view);
//        }
//
//        @Override
//        public void onBindViewHolder(final UserSearchItemView holder, int position) {
//            holder.user = users.get(position);
//            holder.nameView.setText(holder.user.name);
//            holder.supervisorNameView.setText(holder.user.supervisorName);
//            holder.roleNameView.setText(holder.user.roleName);
//
//            holder.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onUserSelected(holder.user);
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return users.size();
//        }
//    }

    public class CustomZXingScannerView extends View implements IViewFinder {
        private static final String TAG = "ViewFinderView";

        private Rect mFramingRect;

        private static final float PORTRAIT_WIDTH_RATIO = 6f/8;
        private static final float PORTRAIT_WIDTH_HEIGHT_RATIO = 1.1f;

        private static final float LANDSCAPE_HEIGHT_RATIO = 5f/8;
        private static final float LANDSCAPE_WIDTH_HEIGHT_RATIO = 1.4f;
        private static final int MIN_DIMENSION_DIFF = 50;

        private static final float SQUARE_DIMENSION_RATIO = 5f/8;

        private final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
        private int scannerAlpha;
        private static final int POINT_SIZE = 10;
        private static final long ANIMATION_DELAY = 80L;

        private final int mDefaultLaserColor = getResources().getColor(R.color.colorAccent);
        private final int mDefaultMaskColor = getResources().getColor(R.color.viewfinder_mask);
        private final int mDefaultBorderColor = getResources().getColor(R.color.colorPrimary);
        private final int mDefaultBorderStrokeWidth = getResources().getInteger(R.integer.viewfinder_border_width);
        private final int mDefaultBorderLineLength = getResources().getInteger(R.integer.viewfinder_border_length);

        protected Paint mLaserPaint;
        protected Paint mFinderMaskPaint;
        protected Paint mBorderPaint;
        protected int mBorderLineLength;
        protected boolean mSquareViewFinder;

        public CustomZXingScannerView(Context context) {
            super(context);
            init();
        }

        public CustomZXingScannerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            //set up laser paint
            mLaserPaint = new Paint();
            mLaserPaint.setColor(mDefaultLaserColor);
            mLaserPaint.setStyle(Paint.Style.FILL);

            //finder mask paint
            mFinderMaskPaint = new Paint();
            mFinderMaskPaint.setColor(mDefaultMaskColor);

            //border paint
            mBorderPaint = new Paint();
            mBorderPaint.setColor(mDefaultBorderColor);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);

            mBorderLineLength = mDefaultBorderLineLength;
        }

        public void setLaserColor(int laserColor) {
            mLaserPaint.setColor(laserColor);
        }
        public void setMaskColor(int maskColor) {
            mFinderMaskPaint.setColor(maskColor);
        }
        public void setBorderColor(int borderColor) {
            mBorderPaint.setColor(borderColor);
        }
        public void setBorderStrokeWidth(int borderStrokeWidth) {
            mBorderPaint.setStrokeWidth(borderStrokeWidth);
        }
        public void setBorderLineLength(int borderLineLength) {
            mBorderLineLength = borderLineLength;
        }

        @Override
        public void setLaserEnabled(boolean b) {

        }

        @Override
        public void setBorderCornerRounded(boolean b) {

        }

        @Override
        public void setBorderAlpha(float v) {

        }

        @Override
        public void setBorderCornerRadius(int i) {

        }

        @Override
        public void setViewFinderOffset(int i) {

        }

        // TODO: Need a better way to configure this. Revisit when working on 2.0
        public void setSquareViewFinder(boolean set) {
            mSquareViewFinder = set;
        }

        public void setupViewFinder() {
            updateFramingRect();
            invalidate();
        }

        public Rect getFramingRect() {
            return mFramingRect;
        }

        @Override
        public void onDraw(Canvas canvas) {
            if(getFramingRect() == null) {
                return;
            }

            drawViewFinderMask(canvas);
            drawViewFinderBorder(canvas);
            drawLaser(canvas);
        }

        public void drawViewFinderMask(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            Rect framingRect = getFramingRect();

            canvas.drawRect(0, 0, width, framingRect.top, mFinderMaskPaint);
            canvas.drawRect(0, framingRect.top, framingRect.left, framingRect.bottom + 1, mFinderMaskPaint);
            canvas.drawRect(framingRect.right + 1, framingRect.top, width, framingRect.bottom + 1, mFinderMaskPaint);
            canvas.drawRect(0, framingRect.bottom + 1, width, height, mFinderMaskPaint);
        }

//        canvas.drawRect(0, 0, width, framingRect.top -30, mFinderMaskPaint);
//        canvas.drawRect(0, framingRect.top -30, framingRect.left, framingRect.bottom + 30, mFinderMaskPaint);
//        canvas.drawRect(framingRect.right - 1, framingRect.top -30, width, framingRect.bottom + 30, mFinderMaskPaint);
//        canvas.drawRect(0, framingRect.bottom + 30, width, height, mFinderMaskPaint);
//    }

        public void drawViewFinderBorder(Canvas canvas) {
            Rect framingRect = getFramingRect();
            canvas.drawLine(framingRect.left - 1, framingRect.top - 1, framingRect.left - 1, framingRect.top - 1 + mBorderLineLength, mBorderPaint);
            canvas.drawLine(framingRect.left - 1, framingRect.top - 1, framingRect.left - 1 + mBorderLineLength, framingRect.top - 1, mBorderPaint);

            canvas.drawLine(framingRect.left - 1, framingRect.bottom + 1, framingRect.left - 1, framingRect.bottom + 1 - mBorderLineLength, mBorderPaint);
            canvas.drawLine(framingRect.left - 1, framingRect.bottom + 1, framingRect.left - 1 + mBorderLineLength, framingRect.bottom + 1, mBorderPaint);

            canvas.drawLine(framingRect.right + 1, framingRect.top - 1, framingRect.right + 1, framingRect.top - 1 + mBorderLineLength, mBorderPaint);
            canvas.drawLine(framingRect.right + 1, framingRect.top - 1, framingRect.right + 1 - mBorderLineLength, framingRect.top - 1, mBorderPaint);

            canvas.drawLine(framingRect.right + 1, framingRect.bottom + 1, framingRect.right + 1, framingRect.bottom + 1 - mBorderLineLength, mBorderPaint);
            canvas.drawLine(framingRect.right + 1, framingRect.bottom + 1, framingRect.right + 1 - mBorderLineLength, framingRect.bottom + 1, mBorderPaint);
        }

//        canvas.drawLine(framingRect.left - 1, framingRect.top - 30, framingRect.left - 1, framingRect.top - 30 + mBorderLineLength, mBorderPaint);
//        canvas.drawLine(framingRect.left - 1, framingRect.top - 30, framingRect.left - 1 + mBorderLineLength, framingRect.top - 30, mBorderPaint);
//
//        canvas.drawLine(framingRect.left - 1, framingRect.bottom + 30, framingRect.left - 1, framingRect.bottom + 30 - mBorderLineLength, mBorderPaint);
//        canvas.drawLine(framingRect.left - 1, framingRect.bottom + 30, framingRect.left - 1 + mBorderLineLength, framingRect.bottom + 30, mBorderPaint);
//
//        canvas.drawLine(framingRect.right + 1, framingRect.top - 30, framingRect.right + 1, framingRect.top - 30 + mBorderLineLength, mBorderPaint);
//        canvas.drawLine(framingRect.right + 1, framingRect.top - 30, framingRect.right + 1 - mBorderLineLength, framingRect.top - 30, mBorderPaint);
//
//        canvas.drawLine(framingRect.right + 1, framingRect.bottom + 30, framingRect.right + 1, framingRect.bottom + 30 - mBorderLineLength, mBorderPaint);
//        canvas.drawLine(framingRect.right + 1, framingRect.bottom + 30, framingRect.right + 1 - mBorderLineLength, framingRect.bottom + 30, mBorderPaint);
//    }

        //    public void drawLaser(Canvas canvas) {
//        Rect framingRect = getFramingRect();
//
//        // Draw a red "laser scanner" line through the middle to show decoding is active
//        mLaserPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
//        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
//        int middle = framingRect.height() / 2 + framingRect.top;
//        canvas.drawRect(framingRect.left + 2, middle - 1, framingRect.right - 1, middle + 2, mLaserPaint);
//
//        postInvalidateDelayed(ANIMATION_DELAY,
//                framingRect.left - POINT_SIZE,
//                framingRect.top - POINT_SIZE,
//                framingRect.right + POINT_SIZE,
//                framingRect.bottom + POINT_SIZE);
//    }
        private int cntr = 0;
        private boolean goingup = false;
        public void drawLaser(Canvas canvas) {
            // Draw a red "laser scanner" line through the middle to show decoding is active
            mLaserPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            int middle = mFramingRect.height() / 2 + mFramingRect.top;
            middle = middle + cntr;
            if ((cntr < 240) && (!goingup)) {
                canvas.drawRect(mFramingRect.left + 2, middle - 1, mFramingRect.right - 1, middle + 2, mLaserPaint);
                cntr = cntr + 40;
            }

            if ((cntr >= 240) && (!goingup)) goingup = true;

            if ((cntr > -200) && (goingup)) {
                canvas.drawRect(mFramingRect.left + 2, middle - 1, mFramingRect.right - 1, middle + 2, mLaserPaint);
                cntr = cntr - 40;
            }

            if ((cntr <= -200) && (goingup)) goingup = false;

            postInvalidateDelayed(ANIMATION_DELAY,
                    mFramingRect.left - POINT_SIZE,
                    mFramingRect.top - POINT_SIZE,
                    mFramingRect.right + POINT_SIZE,
                    mFramingRect.bottom + POINT_SIZE);
        }

        @Override
        protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
            updateFramingRect();
        }

        public synchronized void updateFramingRect() {
            Point viewResolution = new Point(getWidth(), getHeight());
            int width;
            int height;
            int orientation = DisplayUtils.getScreenOrientation(getContext());

            if(mSquareViewFinder) {
                if(orientation != Configuration.ORIENTATION_PORTRAIT) {
                    height = (int) (getHeight() * SQUARE_DIMENSION_RATIO);
                    width = height;
                } else {
                    width = (int) (getWidth() * SQUARE_DIMENSION_RATIO);
                    height = width;
                }
            } else {
                if(orientation != Configuration.ORIENTATION_PORTRAIT) {
                    height = (int) (getHeight() * LANDSCAPE_HEIGHT_RATIO);
                    width = (int) (LANDSCAPE_WIDTH_HEIGHT_RATIO * height);
                } else {
                    width = (int) (getWidth() * PORTRAIT_WIDTH_RATIO);
                    height = (int) (PORTRAIT_WIDTH_HEIGHT_RATIO * width);
                }
            }

            if(width > getWidth()) {
                width = getWidth() - MIN_DIMENSION_DIFF;
            }

            if(height > getHeight()) {
                height = getHeight() - MIN_DIMENSION_DIFF;
            }

            int leftOffset = (viewResolution.x - width) / 2;
            int topOffset = (viewResolution.y - height) / 2;
            mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        }
    }
}