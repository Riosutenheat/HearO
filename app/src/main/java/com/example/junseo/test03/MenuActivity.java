package com.example.junseo.test03;

import com.example.junseo.test03.utils.Constants;
import com.example.junseo.test03.utils.RecycleUtils;
import com.example.junseo.test03.utils.AppSettings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.junseo.test03.utils.Logs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // Refresh timer
    private Timer mRefreshTimer = null;
    private Context mContext;
    private Button buttonLogout;
    private String Alarm_flag = "True";
    private Button button3;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // 기본 루트 레퍼런스
    private FirebaseAuth firebaseAuth;
    private String flag1 = "True";
    private static final String TAG = MenuActivity.class.getSimpleName();

    Vibrator mVibe; //진동
    Button blinking_animation = null; // 화재 애니메이션
    Button start = null; // 화재 애니메이션 시작
    Button blinking_animation2 = null; // 노크 애니메이션
    Button start2 = null; // 노크 애니메이션 시작
    Button blinking_animation3 = null; // 음성 애니메이션
    Button start3 = null; // 음성 애니메이션 시작
    Button blinking_animation4 = null; // 초인종 애니메이션
    Button start4 = null; // 초인종 애니메이션
    TextView firetext = null; // 불났어요 출력
    TextView doortext = null; // 노크 출력
    TextView voicetext = null; // 음성 출력
    TextView belltext = null; // 초인종 애니메이션
    LinearLayout back;
    boolean flag = FALSE;   //진동 울리면 TRUE 평소에 FALSE
    boolean dbflag = FALSE;   //데이터 추가되면 TRUE 평소에 FALSE

    int[] img = {R.drawable.hback1}; // 여기서  , , , 식으로 추가를 하면 랜덤으로 뽑아옴
    FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
    final String email = user.getEmail();
    int i = email.indexOf("@");
    String id = email.substring(0, i);
    final DatabaseReference User_Alarm = databaseReference.child("huser").child(id).child("sensor").child("Alarm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //----- System, Context
        mContext = this;    //.getApplicationContext(); context를 가져와야만 실행이 되는 것 같은데..
        AppSettings.initializeAppSettings(mContext);

        //배경 랜덤 설정
        back = (LinearLayout) findViewById(R.id.layout);
        Random ram = new Random();
        int num = ram.nextInt(img.length);
        back.setBackgroundResource(img[num]);
        blinking_animation = (Button) findViewById(R.id.blinking_animation);

        start = (Button) findViewById(R.id.start);
        firetext = (TextView) findViewById(R.id.firetext);
        mVibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        blinking_animation2 = (Button) findViewById(R.id.blinking_animation2);
        start2 = (Button) findViewById(R.id.start2);
        doortext = (TextView) findViewById(R.id.doortext);

        blinking_animation3 = (Button) findViewById(R.id.blinking_animation3);
        start3 = (Button) findViewById(R.id.start3);
        voicetext = (TextView) findViewById(R.id.voice);

        blinking_animation4 = (Button) findViewById(R.id.blinking_animation4);
        start4 = (Button) findViewById(R.id.start4);
        belltext = (TextView) findViewById(R.id.bell);


        blinking_animation.setOnClickListener(this);
        start.setOnClickListener(this);
        blinking_animation2.setOnClickListener(this);
        start2.setOnClickListener(this);
        blinking_animation3.setOnClickListener(this);
        start3.setOnClickListener(this);
        blinking_animation4.setOnClickListener(this);
        start4.setOnClickListener(this);

/*        mImageBT = (ImageView) findViewById(R.id.status_title);
        mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
        mTextStatus = (TextView) findViewById(R.id.status_text);
        mTextStatus.setText(getResources().getString(R.string.bt_state_init)); // 블루투스 상태*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ToggleButton tb = (ToggleButton) findViewById(R.id.HearMainbutton);
        final Button button1 = (Button) findViewById(R.id.button1);
        //텍스트 입력 버튼/
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ttsIntent = new Intent(MenuActivity.this, ChatActivity.class);
                MenuActivity.this.startActivity(ttsIntent);
            }
        });
        final Button button2 = (Button) findViewById(R.id.button2);
        //상용구 버튼
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent macroIntent = new Intent(MenuActivity.this, MacroActivity.class);
                MenuActivity.this.startActivity(macroIntent);
            }
        });
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);
        //STT 버튼
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sttIntent = new Intent(MenuActivity.this, STTActivity.class);
                MenuActivity.this.startActivity(sttIntent);

            }
        });

        //  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
      /*  fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        tb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (tb.isChecked()) {
                    tb.setBackgroundDrawable(getResources().getDrawable(R.drawable.hearobutton));
                } else {
                    tb.setBackgroundDrawable(getResources().getDrawable(R.drawable.hearobutton));
                }
            }
        });
        //메인 버튼 이미지 토글.
        if (tb.isChecked()) {
            tb.setBackgroundDrawable(getResources().getDrawable(R.drawable.hearobuttonon));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //buttonLogout = (Button) findViewById(R.id.b+uttonLogout);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));

        }



    }
    @Override
    protected void onStart() {
        super.onStart();

        // false가 아닐 때만 noti를 실행해야 한다...
        User_Alarm.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // val 값을 가져온다.
                if(Objects.equals(value, flag1)){
                    pushnoti(); // 노티피케이션
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    public void Vibrator_pattern() {

        long[] vibratePattern = {100, 100, 100};
        mVibe.vibrate(vibratePattern, 0);
        flag = TRUE;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        if (flag == TRUE) {
            mVibe.cancel();
            flag = FALSE;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // This prevents reload after configuration changes
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finalizeActivity();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // onDestroy is not always called when applications are finished by Android system.
        finalizeActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_scan) {
            return true;
        }
        /*
       if(id== R.id.action_discoverable) // 이건 왠지 필요없을 듯.
        {
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onUserLeaveHint() {  //홈버튼 이벤트
//여기서 감지
        Log.d("한솔", "Home Button Touch");
        if (flag == TRUE) {
            mVibe.cancel();
            flag = FALSE;
        }
        super.onUserLeaveHint();
    }


    public void NotificationFire() {


        Resources res = getResources();


        Intent notificationIntent = new Intent(this, MenuActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);// 루트 액티비티 하나만 뜨게 플래그 잡아줌

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("HearO")
                .setContentText("불이 났나봐요!")
                .setTicker("화재")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }

    public void NotificationDoor() {
        Resources res = getResources();
        Intent notificationIntent = new Intent(this, MenuActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP); // 루트 액티비티 하나만 뜨게 플래그 잡아줌
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("HearO")
                .setContentText("똑똑! 누군가가 문을 두드렸어요!")
                .setTicker("노크")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }

    public void Notificationbell() {
        Resources res = getResources();
        Intent notificationIntent = new Intent(this, MenuActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP); // 루트 액티비티 하나만 뜨게 플래그 잡아줌
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("HearO")
                .setContentText("띵동! 택배라도 왔나?")
                .setTicker("초인종")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }

    public void onknock() {
        User_Alarm.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // val 값을 가져온다.
                if(Objects.equals(value, flag1)){
                    NotificationDoor();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mVibe.cancel();
        blinking_animation.clearAnimation();
        blinking_animation3.setVisibility(View.INVISIBLE);
        blinking_animation4.setVisibility(View.INVISIBLE);
        firetext.setVisibility(View.GONE);
        voicetext.setVisibility(View.GONE);
        belltext.setVisibility(View.GONE);

        blinking_animation2.setVisibility(View.VISIBLE);
        doortext.setVisibility(View.VISIBLE);

    }

    public void onfire() {
        User_Alarm.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // val 값을 가져온다.
                if(Objects.equals(value, flag1)){
                    NotificationFire();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        blinking_animation4.setVisibility(View.INVISIBLE);
        blinking_animation3.setVisibility(View.INVISIBLE);
        blinking_animation2.setVisibility(View.INVISIBLE);
        doortext.setVisibility(View.GONE);
        voicetext.setVisibility(View.GONE);
        belltext.setVisibility(View.GONE);
        Vibrator_pattern();
        Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blinking_animation);
        blinking_animation.startAnimation(startAnimation);
        firetext.setVisibility(View.VISIBLE);

    }

    /*public void onvoice()
    {

        mVibe.cancel();
        blinking_animation.clearAnimation();
        firetext.setVisibility(View.GONE);
        blinking_animation2.setVisibility(View.GONE);
        doortext.setVisibility(View.GONE);
        blinking_animation4.setVisibility(View.INVISIBLE);
        belltext.setVisibility(View.GONE);

        blinking_animation3.setVisibility(View.VISIBLE);
        voicetext.setVisibility(View.VISIBLE);
    }*/
    public void onbell() {
        Notificationbell();
        mVibe.cancel();
        blinking_animation.clearAnimation();
        firetext.setVisibility(View.GONE);
        blinking_animation2.setVisibility(View.GONE);
        doortext.setVisibility(View.GONE);
        blinking_animation3.setVisibility(View.INVISIBLE);
        voicetext.setVisibility(View.GONE);
        blinking_animation4.setVisibility(View.VISIBLE);
        belltext.setVisibility(View.VISIBLE);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_myinfo) {
            startActivity(new Intent(this, ProfileActivity.class)); //내 정보
        } else if (id == R.id.nav_version) {
            startActivity(new Intent(this, VersionActivity.class)); //앱 버전
        } else if (id == R.id.nav_help) {
            startActivity(new Intent(this, HelpActivity.class)); //도움말
        } else if (id == R.id.nav_module) {
            startActivity(new Intent(this, StartActivity.class)); //모듈 연결/해제    //1003 네비게이션 수정


        } else if (id == R.id.nav_alert) {
            finish();
            startActivity(new Intent(this, AlarmActivity.class)); //알림 설정
        } else if (id == R.id.Logout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class)); //로그아웃
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

  /*  @Override
    public synchronized void onStart() {
        super.onStart();

    }*/

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        // Stop the timer
        if (mRefreshTimer != null) {
            mRefreshTimer.cancel();
            mRefreshTimer = null;
        }
        super.onStop();
    }

    @Override
    public void onClick(View view) {
/*        if(view == button3)
        {
            callFragment(FRAGMENT_STT);
        }*/
        switch (view.getId()) {
            case R.id.blinking_animation:
                blinking_animation.clearAnimation();
                firetext.setVisibility(View.GONE);
                mVibe.cancel();
                break;

            case R.id.blinking_animation2:
                blinking_animation2.setVisibility(View.INVISIBLE);
                doortext.setVisibility(View.GONE);
                break;

            case R.id.blinking_animation3:
                blinking_animation3.setVisibility(View.INVISIBLE);
                voicetext.setVisibility(View.GONE);
                break;

            case R.id.blinking_animation4:
                blinking_animation4.setVisibility(View.INVISIBLE);
                belltext.setVisibility(View.GONE);

                break;

        }

    }

    private void initialize() {
        Logs.d(TAG, "# Activity - initialize()");

        // Load activity reports and display
        if (mRefreshTimer != null) {
            mRefreshTimer.cancel();
        }

        // Use below timer if you want scheduled job
        //mRefreshTimer = new Timer();
        //mRefreshTimer.schedule(new RefreshTimerTask(), 5*1000);
    }

    private void finalizeActivity() {
        Logs.d(TAG, "# Activity - finalizeActivity()");

        // Clean used resources
        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();
    }

    public void pushnoti() {
        // 리스트 어댑터 생성 및 세팅

        final Resources res = getResources();
        Intent notificationIntent = new Intent(this, MenuActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);// 루트 액티비티 하나만 뜨게 플래그 잡아줌
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id = email.substring(0, i);

        final DatabaseReference sensordb = databaseReference.child("huser").child(id).child("sensor");
        final DatabaseReference knockdb = sensordb.child("knock");
        final DatabaseReference firedb = sensordb.child("fire");
        final DatabaseReference voicedb = sensordb.child("voice");
        final DatabaseReference voiceSend = sensordb.child("voice").child("text");
        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
                    knockdb.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            Log.e("LOG", "dataSnapshot.getKey() : " + dataSnapshot.getKey());
                            onknock();
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    firedb.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            Log.e("LOG", "dataSnapshot.getKey() : " + dataSnapshot.getKey());
                            onfire();
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    voicedb.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildChanged(final DataSnapshot dataSnapshot, String s) {

                            User_Alarm.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot1) {
                                    String value = dataSnapshot1.getValue(String.class); // val 값을 가져온다.
                                    if(Objects.equals(value, flag1)){

                                       voiceSend.addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot2) {
                                               Log.e("LOG", "dataSnapshot.getKey() : " + dataSnapshot.getKey());
                                               Log.e("LOG", "dataSnapshot.getValue() : " + dataSnapshot.getValue());
                                               Log.e("LOG", "dataSnapshot.child('text').getValue() :" + dataSnapshot2.getValue());
                                               builder.setContentTitle("HearO")
                                                       .

                                                               setContentText((String) dataSnapshot2.getValue())
                                                       .

                                                               setTicker("음성")
                                                       .

                                                               setSmallIcon(R.mipmap.ic_launcher)
                                                       .

                                                               setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                                                       .

                                                               setContentIntent(contentIntent)
                                                       .

                                                               setAutoCancel(true)
                                                       .

                                                               setWhen(System.currentTimeMillis())
                                                       .

                                                               setDefaults(Notification.DEFAULT_SOUND);
                                               voicetext.setText((String) dataSnapshot2.getValue());

                                               if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)

                                               {
                                                   builder.setCategory(Notification.CATEGORY_MESSAGE)
                                                           .setPriority(Notification.PRIORITY_HIGH)
                                                           .setVisibility(Notification.VISIBILITY_PUBLIC);
                                               }

                                               NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                               nm.notify(1234, builder.build());
                                               mVibe.cancel();
                                               blinking_animation.clearAnimation();
                                               firetext.setVisibility(View.GONE);
                                               blinking_animation2.setVisibility(View.GONE);
                                               doortext.setVisibility(View.GONE);
                                               blinking_animation4.setVisibility(View.INVISIBLE);
                                               belltext.setVisibility(View.GONE);

                                               blinking_animation3.setVisibility(View.VISIBLE);
                                               voicetext.setVisibility(View.VISIBLE);
                                           }
                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {
                                           }
                                       });

                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });


                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }

                    });
                }
            }




