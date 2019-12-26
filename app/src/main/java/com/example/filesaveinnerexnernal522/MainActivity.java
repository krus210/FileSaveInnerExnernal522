package com.example.filesaveinnerexnernal522;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerLanguage;
    private EditText editLogin;
    private EditText editPassword;
    private CheckBox checkBox;
    public static final int REQUEST_CODE_PERMISSION_WRITE_STORAGE = 11;
    public static final String APP_PREFERENCES = "my settings";
    public static final String CHECKBOX_STATUS = "checkBox status";
    public static final String INNER_FILE = "inner_file.txt";
    public static final String EXTERNAL_FILE = "external_file.txt";
    SharedPreferences mSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        editLogin = findViewById(R.id.editLogin);
        editPassword = findViewById(R.id.editPassword);
        Button buttonOk = findViewById(R.id.buttonOK);
        Button buttonRegistration = findViewById(R.id.buttonRegistration);
        checkBox = findViewById(R.id.checkBox);

        if(mSettings.contains(CHECKBOX_STATUS)) {
            boolean status = mSettings.getBoolean(CHECKBOX_STATUS,false);
            checkBox.setChecked(status);
        }

        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this,
                R.array.changeLanguage,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saveStatusCheckBox(true);
                    int permissionStatus = ContextCompat.
                            checkSelfPermission(MainActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION_WRITE_STORAGE);
                    }
                } else {
                    saveStatusCheckBox(false);
                }
            }
        });

        buttonRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = editLogin.getText().toString();
                String password = editPassword.getText().toString();
                if (login.equals("") || password.equals("")) {
                    toastZeroEdit();
                } else {
                    if (checkBox.isChecked()) {
                        writeExternalFile(login, password);
                    } else {
                        writeInnerFile(login, password);
                    }
                }
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = editLogin.getText().toString();
                String password = editPassword.getText().toString();
                String loginSave = "";
                String passwordSave = "";
                if (login.equals("") || password.equals("")) {
                    toastZeroEdit();
                } else {
                    if (checkBox.isChecked()) {
                        loginSave = readExternalFile()[0];
                        passwordSave = readExternalFile()[1];
                    } else {
                        loginSave = readInnerFile()[0];
                        passwordSave = readInnerFile()[1];
                    }
                    if (login.equals(loginSave) && password.equals(passwordSave)) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.data_equal),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.data_not_equal),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    private void writeInnerFile(String login, String password) {
        try {
            FileOutputStream fileOutputStream = openFileOutput(INNER_FILE, MODE_PRIVATE);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bw = new BufferedWriter(outputStreamWriter);
            bw.write(login + "\n" + password);
            bw.close();
            Toast.makeText(MainActivity.this,
                    getString(R.string.success_inner_write),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this,
                    getString(R.string.error_inner_write),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String[] readInnerFile() {
        String[] loginPassword = new String[2];
        try {
            FileInputStream fileInputStream = openFileInput(INNER_FILE);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                loginPassword[count] = line;
                count++;
            }
            br.close();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this,
                    getString(R.string.error_inner_read),
                    Toast.LENGTH_SHORT).show();
        }
        return loginPassword;
    }

    private void writeExternalFile(String login, String password) {
        if (isExternalStorageWritable()) {
            File fileAdd = new File(getApplicationContext().
                    getExternalFilesDir(null),
                    EXTERNAL_FILE);
            try {
                // открываем поток для записи
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileAdd, true));
                // пишем данные
                bw.write(login + "\n" + password);
                // закрываем поток
                bw.close();
                Toast.makeText(MainActivity.this,
                        getString(R.string.success_external_write),
                        Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_external_write),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,
                    getString(R.string.error_add_external_file),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String[] readExternalFile() {
        String[] loginPassword = new String[2];
        if (isExternalStorageReadable()) {
            File fileRead = new File(getApplicationContext().
                    getExternalFilesDir(null),
                    EXTERNAL_FILE);
            try {
                // открываем поток для чтения
                BufferedReader br = new BufferedReader(new FileReader(fileRead));
                // читаем содержимое
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    loginPassword[count] = line;
                    count++;
                }
                br.close();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.error_external_read),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,
                    getString(R.string.error_read_external_file),
                    Toast.LENGTH_LONG).show();
        }
        return loginPassword;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_WRITE_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    saveStatusCheckBox(true);
                    Toast.makeText(this,
                            getString(R.string.permission_granted),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied
                    checkBox.setChecked(false);
                    saveStatusCheckBox(false);
                    Toast.makeText(this,
                            getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }


    public void btnSelectLanguageClick(View view) {
        String selected = spinnerLanguage.getSelectedItem().toString();
        String[] languages = getResources().getStringArray(R.array.changeLanguage);
        Locale locale;
        String englishLanguage = languages[1];
        if (selected.equals(englishLanguage)) {
            locale = new Locale("en");
        } else {
            locale = new Locale("ru");
        }
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(
                config,
                getBaseContext().
                        getResources().
                        getDisplayMetrics());
        recreate();
    }

    private void saveStatusCheckBox(boolean statusCheckBox) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(CHECKBOX_STATUS, statusCheckBox);
        editor.apply();
    }

    private void toastZeroEdit() {
        Toast.makeText(MainActivity.this,
                getString(R.string.zero_login_password),
                Toast.LENGTH_SHORT).show();
    }

}

