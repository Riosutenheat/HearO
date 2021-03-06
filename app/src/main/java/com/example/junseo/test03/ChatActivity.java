package com.example.junseo.test03;

/**
 * Created by KHR on 2017-08-09.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    TextToSpeech tts;
    private ListView chat_view;
    private EditText chat_edit;
    private Button chat_send;
    Button Cancel10;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // 기본 루트 레퍼런스
    private FirebaseAuth firebaseAuth; // 파이어베이스 인증

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() { // tts 처리
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN); // 한국어로 tts처리
                }
            }
        });
        // 위젯 ID 참조
        chat_view = (ListView) findViewById(R.id.chat_view);
        chat_edit = (EditText) findViewById(R.id.chat_edit);
        chat_send = (Button) findViewById(R.id.chat_sent);
        Cancel10 = (Button) findViewById(R.id.Cancel10);
        // 로그인 화면에서 받아온 채팅방 이름, 유저 이름 저장
        //final Intent intent = getIntent();

        // 채팅 방 입장
        openChat();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id = email.substring(0,i);
        final String userid = user.getUid();

        final DatabaseReference chatdatabaseReference = FirebaseDatabase.getInstance().getReference().child("huser").child(id).child("chat"); // 채팅 레퍼런스
        final DatabaseReference dashboard = FirebaseDatabase.getInstance().getReference().child("hdashboard").child(userid).child("chat"); // 대쉬보드 레퍼런스
        final DatabaseReference tts_firebase = FirebaseDatabase.getInstance().getReference().child("huser").child(id);
        // 메시지 전송 버튼에 대한 클릭 리스너 지정

        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                final String email = user.getEmail();
                int i = email.indexOf("@");
                String id = email.substring(0,i);
                int position = chat_view.getFirstVisiblePosition(); // chat list 위치
                if (chat_edit.getText().toString().equals(""))
                    return;
                String text = chat_edit.getText().toString();
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                //http://stackoverflow.com/a/29777304
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(text);
                } else {
                    ttsUnder20(text);
                }
                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E요일");
                // nowDate 변수에 값을 저장한다.
                String chattime = sdfNow.format(date);
                final DatabaseReference chatpushedPostRefkey = chatdatabaseReference.push();
                final String chatkey = chatpushedPostRefkey.getKey();
                final ChatDTO chat = new ChatDTO(chat_edit.getText().toString()); //ChatDTO를 이용하여 데이터를 묶는다.

                // firebase 저장
                SimpleDateFormat yo = new SimpleDateFormat("E요일");
                String yoman = yo.format(date);
                chatdatabaseReference.child(chatkey).setValue(chat); // 데이터 푸쉬
                dashboard.child(chattime).child(yoman).setValue(chattime);
                tts_firebase.child("chat_tts").child("text").setValue(text);
                tts_firebase.child("chat_tts").child("time").setValue(chattime);
                chat_edit.setText(""); //입력창 초기화
                chat_view.smoothScrollToPosition(position);

            }
        });
        Cancel10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatdatabaseReference.removeValue();
                DatabaseReference chatpushedPostRefkey = chatdatabaseReference.push();
                String chatkey = chatpushedPostRefkey.getKey();
                ChatDTO chat = new ChatDTO("전송할 내용을 입력하세요");
                chatdatabaseReference.child(chatkey).setValue(chat);

                Log.e("LOG","removemessage");

                finish();
                //Intent intent = new Intent(ChatActivity.this, MenuActivity.class);
                //startActivity(intent);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
    }
    private void addMessage(DataSnapshot dataSnapshot, ArrayAdapter<String> adapter) {
        ChatDTO chatDTO = dataSnapshot.getValue(ChatDTO.class);
        adapter.add(chatDTO.getMessage());
    }

    private void removeMessage(DataSnapshot dataSnapshot, ArrayAdapter<String> adapter) {
        ChatDTO chatDTO = dataSnapshot.getValue(ChatDTO.class);
        adapter.remove(chatDTO.getMessage());
    }


    private void openChat() {
        // 리스트 어댑터 생성 및 세팅
        final ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        chat_view.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // @앞 까지만 불러오기
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id = email.substring(0,i);


        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
        databaseReference.child("huser").child(id).child("chat").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addMessage(dataSnapshot, adapter);
                Log.e("LOG", "s:"+s);

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onChildRemoved(final DataSnapshot dataSnapshot) {
                Log.e("LOG","removemessage");
                removeMessage(dataSnapshot, adapter);
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