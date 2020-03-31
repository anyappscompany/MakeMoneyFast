package ua.com.anyapps.makemoneyfast.ui.earn_money;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EarnMoneyViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public EarnMoneyViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Earn Monay fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}