package ua.com.anyapps.makemoneyfast.ui.finance;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ua.com.anyapps.makemoneyfast.CompassActivity;
import ua.com.anyapps.makemoneyfast.EmailPasswordActivity;
import ua.com.anyapps.makemoneyfast.MainActivity;
import ua.com.anyapps.makemoneyfast.MyFunctions;
import ua.com.anyapps.makemoneyfast.R;

public class FinanceFragment extends Fragment {

    private FinanceViewModel financeViewModel;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    private static final String TAG = "debapp";
    ProgressBar pbLoading;
    Context context;

    TextView tvBalanceHeading;
    TextView tvBalance;
    EditText etCardNumber;
    Button btnPayoutOrder;
    ImageView ivVisaLogo;
    ImageView ivMasterCardLogo;

    private FirebaseFunctions mFunctions;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        financeViewModel =
                ViewModelProviders.of(this).get(FinanceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_finance, container, false);
        tvBalanceHeading = root.findViewById(R.id.tvBalanceHeading);
        tvBalance = root.findViewById(R.id.tvBalance);
        etCardNumber = root.findViewById(R.id.etCardNumber);
        btnPayoutOrder = root.findViewById(R.id.btnPayoutOrder);
        ivVisaLogo = root.findViewById(R.id.ivVisaLogo);
        ivMasterCardLogo = root.findViewById(R.id.ivMasterCardLogo);

        pbLoading = (ProgressBar)root.findViewById(R.id.pbLoading);
        pbLoading.setVisibility(View.VISIBLE);
        tvBalanceHeading.setVisibility(View.GONE);
        tvBalance.setVisibility(View.GONE);
        etCardNumber.setVisibility(View.GONE);
        btnPayoutOrder.setVisibility(View.GONE);
        ivVisaLogo.setVisibility(View.GONE);
        ivMasterCardLogo.setVisibility(View.GONE);

        mFunctions = FirebaseFunctions.getInstance();

        /*financeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        //updateBalance();

        btnPayoutOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {


                Log.d(TAG,"--------" + totalMoney + "-----" + minimumForWithdrawal);

                if(totalMoney<minimumForWithdrawal){
                    Toast.makeText(context, getResources().getString(R.string.no_minimal_sum_text) + " " + minimumForWithdrawal, Toast.LENGTH_LONG).show();
                }

                final float balance = totalMoney;
                Log.d(TAG, "BalanseInt: " +balance);
                Log.d(TAG, "Время нажатия: "+ServerValue.TIMESTAMP);
                if(etCardNumber.getText().toString().length()==16 && balance>0) {

                    // Если в базе есть уже необработанная заявка на выплату, то отклонить запрос, иначе создать заявку
                    DatabaseReference firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("payouts").child(user.getUid());
                    firebaseDatabaseReference.orderByChild("status").equalTo(0).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Log.d(TAG, "Total: "+dataSnapshot.getChildrenCount());
                            if(dataSnapshot.getChildrenCount()>=1) {
                                Log.d(TAG, "Заявка Уже есть!");
                                Toast.makeText(getActivity(), getResources().getString(R.string.duplicate_withdrawal_request_money), Toast.LENGTH_LONG).show();
                            }else{
                                // заявка на вывод денег
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("payouts").child(user.getUid()).push();
                                myRef.child("amount").setValue(balance);
                                myRef.child("cardnumber").setValue(etCardNumber.getText().toString());
                                myRef.child("status").setValue(0);
                                myRef.child("addtime").setValue(ServerValue.TIMESTAMP);



                                Toast.makeText(getActivity(), getResources().getString(R.string.request_for_payment_created), Toast.LENGTH_LONG).show();

                                /*String locale = getResources().getConfiguration().locale.getCountry();
                                TelephonyManager tm = (TelephonyManager)getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
                                String countryCodeValue = tm.getNetworkCountryIso();*/
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                            // ошибка при поиске в базе placeId
                            //MyFunctions.hideProgressDialog(mProgressDialog);
                            //Toast.makeText(CompassActivity.this, getResources().getString(R.string.upload_photo_failed_toast_text), Toast.LENGTH_LONG).show();
                            //Log.d(TAG, "Ошибка при поиске placeId в Firebase Database - " +databaseError.getMessage() );
                        }
                    });

                }//MyFunctions.hideProgressDialog(mProgressDialog);
            }
        });
        return root;
    }

    // цена фото для страны
    private float getPrice(float defaultPrice, String countryCode, HashMap<String, Float> pricesList){
        float result = 0;
        boolean naiden = false;

        if(pricesList.containsKey(countryCode)){
            result = pricesList.get(countryCode);
        }else{
            result = defaultPrice;
        }

        return result;
    }


    HashMap<String, Float> clickWeights = new HashMap<>();
    DatabaseReference firebaseDatabaseReference;
    float totalMoney = 0;
    float coefficient = 0;
    float defaultClickWeight = 0;
    long unpaidPhotos = 0;
    long firstPhotoAddTime = 0;
    long minimumForWithdrawal = 0;
    private void updateBalance(){
        Log.d(TAG, "111");
        totalMoney = 0;
        coefficient = 0;
        defaultClickWeight = 0;
        unpaidPhotos = 0;
        firstPhotoAddTime = 0;

        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("minwithdraw");
        firebaseDatabaseReference.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>=1) {
                    Log.d(TAG, "Получен лимит " + dataSnapshot.child("min").getValue().toString());
                    minimumForWithdrawal = Long.valueOf(dataSnapshot.child("min").getValue().toString());

        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("clickweight");
        firebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>=1){
                    defaultClickWeight = Float.valueOf(dataSnapshot.child("def").getValue().toString());
                    //Log.d(TAG, "def: " +dataSnapshot.child("def").getValue());
                    for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, imageSnapshot.getKey());
                        clickWeights.put(imageSnapshot.getKey(), Float.valueOf(imageSnapshot.getValue().toString()));
                        //break;
                    }



                    firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("images").child(user.getUid());
                    firebaseDatabaseReference.orderByChild("paid").equalTo(0).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            unpaidPhotos = dataSnapshot.getChildrenCount();
                            if(unpaidPhotos>=1){
                                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                                    //Log.d(TAG, imageSnapshot.child("countrycode").getValue().toString());
                                    firstPhotoAddTime = Long.valueOf(imageSnapshot.child("addtime").getValue().toString());
                                    //clickWeights.put(imageSnapshot.getKey(), Float.valueOf(imageSnapshot.getValue().toString()));
                                    Log.d(TAG, "Первое фото: " + firstPhotoAddTime);
                                    /*********************************************************/

                                    firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("clicks").child(user.getUid());
                                    firebaseDatabaseReference.orderByChild("clicktime").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            //Log.d(TAG, "Clicks " + dataSnapshot.getChildrenCount());
                                            //unpaidPhotos = dataSnapshot.getChildrenCount();
                                            pbLoading.setVisibility(View.GONE);
                                            tvBalanceHeading.setVisibility(View.VISIBLE);
                                            tvBalance.setVisibility(View.VISIBLE);
                                            etCardNumber.setVisibility(View.VISIBLE);
                                            btnPayoutOrder.setVisibility(View.VISIBLE);
                                            ivVisaLogo.setVisibility(View.VISIBLE);
                                            ivMasterCardLogo.setVisibility(View.VISIBLE);
                                            if(unpaidPhotos>=1){
                                                int validClicks = 0;
                                                for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                                                    Log.d(TAG, imageSnapshot.child("clicktime").getValue().toString() + " compare " + firstPhotoAddTime);
                                                    if(Long.valueOf(imageSnapshot.child("clicktime").getValue().toString())>=firstPhotoAddTime){
                                                        //Log.d(TAG, "GP"+clickWeights.size());
                                                        totalMoney += getPrice(defaultClickWeight,imageSnapshot.child("countrycode").getValue().toString(), clickWeights);
                                                        validClicks++;
                                                    }
                                                }
                                                Log.d(TAG, "Валидных кликов " + validClicks);
                                                Log.d(TAG, "Вскго за клики " + totalMoney);
                                                coefficient = totalMoney/unpaidPhotos;
                                                Log.d(TAG, "Цена за фото  " + coefficient);
                                                //tvBalance.setText(unpaidPhotos + "*"+coefficient);
                                                tvBalance.setText((unpaidPhotos * coefficient)+"");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError)
                                        {
                                            pbLoading.setVisibility(View.GONE);
                                            tvBalanceHeading.setVisibility(View.VISIBLE);
                                            tvBalance.setVisibility(View.VISIBLE);
                                            etCardNumber.setVisibility(View.VISIBLE);
                                            btnPayoutOrder.setVisibility(View.VISIBLE);
                                            ivVisaLogo.setVisibility(View.VISIBLE);
                                            ivMasterCardLogo.setVisibility(View.VISIBLE);
                                            // ошибка при получении списка фото
                                            Log.e(TAG, "Ошибка при получении списка фото для расчета стоимости - " +databaseError.getMessage() );

                                        }
                                    });

                                    /*********************************************************/
                                    break;
                                }
                            }else{
                                pbLoading.setVisibility(View.GONE);
                                tvBalanceHeading.setVisibility(View.VISIBLE);
                                tvBalance.setVisibility(View.VISIBLE);
                                etCardNumber.setVisibility(View.VISIBLE);
                                btnPayoutOrder.setVisibility(View.VISIBLE);
                                ivVisaLogo.setVisibility(View.VISIBLE);
                                ivMasterCardLogo.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                            pbLoading.setVisibility(View.GONE);
                            tvBalanceHeading.setVisibility(View.VISIBLE);
                            tvBalance.setVisibility(View.VISIBLE);
                            etCardNumber.setVisibility(View.VISIBLE);
                            btnPayoutOrder.setVisibility(View.VISIBLE);
                            ivVisaLogo.setVisibility(View.VISIBLE);
                            ivMasterCardLogo.setVisibility(View.VISIBLE);
                            // ошибка при получении списка фото
                            Log.e(TAG, "Ошибка при получении списка фото для расчета стоимости - " +databaseError.getMessage() );

                        }
                    });





                }else{
                    //цены не получены
                    pbLoading.setVisibility(View.GONE);
                    tvBalanceHeading.setVisibility(View.VISIBLE);
                    tvBalance.setVisibility(View.VISIBLE);
                    etCardNumber.setVisibility(View.VISIBLE);
                    btnPayoutOrder.setVisibility(View.VISIBLE);
                    ivVisaLogo.setVisibility(View.VISIBLE);
                    ivMasterCardLogo.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Цены не получены");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                pbLoading.setVisibility(View.GONE);
                tvBalanceHeading.setVisibility(View.VISIBLE);
                tvBalance.setVisibility(View.VISIBLE);
                etCardNumber.setVisibility(View.VISIBLE);
                btnPayoutOrder.setVisibility(View.VISIBLE);
                ivVisaLogo.setVisibility(View.VISIBLE);
                ivMasterCardLogo.setVisibility(View.VISIBLE);
                // ошибка при получении баланса
                Toast.makeText(context, getResources().getString(R.string.get_balance_failed_toast_text), Toast.LENGTH_LONG).show();
                Log.d(TAG, "Ошибка при получении баланса - " +databaseError.getMessage() );

            }
        });


                }else{
                    Log.d(TAG, "Не получен минимум");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // ошибка при поиске в базе placeId
                //MyFunctions.hideProgressDialog(mProgressDialog);
                //Toast.makeText(CompassActivity.this, getResources().getString(R.string.upload_photo_failed_toast_text), Toast.LENGTH_LONG).show();
                //Log.d(TAG, "Ошибка при поиске placeId в Firebase Database - " +databaseError.getMessage() );
            }
        });




    }

    @Override
    public void onResume() {
        super.onResume();
        pbLoading.setVisibility(View.VISIBLE);
        tvBalanceHeading.setVisibility(View.GONE);
        tvBalance.setVisibility(View.GONE);
        etCardNumber.setVisibility(View.GONE);
        btnPayoutOrder.setVisibility(View.GONE);
        ivVisaLogo.setVisibility(View.GONE);
        ivMasterCardLogo.setVisibility(View.GONE);
        updateBalance();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // выход из аккаунта
    public void btnGetCoefficientClick(View v) {
    }
}