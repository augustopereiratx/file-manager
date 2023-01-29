package com.bergamota.filemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        Bundle b = getIntent().getExtras();
        String dirPosition = "/storage/emulated/0"; // or other values
        if (b != null) {
            dirPosition = b.getString("dirBack");
        }
        if (dirPosition != null) {
            startActivity(new Intent(getBaseContext(), MainActivity.class));
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            Bundle bBack = new Bundle();
            bBack.putString("dirBack2", dirPosition);
            intent.putExtras(bBack);
            startActivity(intent);
            finish();
        } else {
            this.finishAffinity();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout1);
        getSupportActionBar().hide();


    }

    class TextAdapter extends BaseAdapter {

        private List<String> data = new ArrayList<>();

        private boolean[] selection;

        public void setData(List<String> data) {
            if (data != null) {
                this.data.clear();
                if (data.size() > 0) {
                    this.data.addAll(data);
                }
                notifyDataSetChanged();
            }
        }

        void setSelection(boolean[] selection) {
            if (selection != null) {
                this.selection = new boolean[selection.length];
                for (int i = 0; i < selection.length; i++) {
                    this.selection[i] = selection[i];
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String item = getItem(position);
            holder.info.setText(item.substring(item.lastIndexOf('/') + 1));
            if (selection != null) {
                if (selection[position]) {
                    holder.info.setBackgroundColor(Color.argb(100, 70, 70, 70));
                } else {
                    int nightModeFlags = parent.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    switch (nightModeFlags) {
                        case Configuration.UI_MODE_NIGHT_YES:
                            holder.info.setBackgroundColor(Color.rgb(20, 20, 20));
                            break;

                        case Configuration.UI_MODE_NIGHT_NO:
                            holder.info.setBackgroundColor(Color.WHITE);
                            break;

                        case Configuration.UI_MODE_NIGHT_UNDEFINED:
                            holder.info.setBackgroundColor(Color.WHITE);
                            break;
                    }
                }
            }
            return convertView;
        }

        class ViewHolder {
            TextView info;

            ViewHolder(TextView info) {
                this.info = info;
            }
        }
    }

    private static final int REQUEST_PERMISSIONS = 1234;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT = 2;

    @SuppressLint("NewApi")
    private boolean arePermissionsDenied() {

        int p = 0;
        while (p < PERMISSIONS_COUNT) {
            if (checkSelfPermission(PERMISSIONS[p]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            p++;
        }

        return false;
    }

    private boolean isFileManagerInitialized = false;

    private boolean[] selection;

    private File[] files;

    private List<String> filesList;

    private int filesFountCount;

    private boolean lClickVerifier = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }
        if (!isFileManagerInitialized) {
            Bundle b = getIntent().getExtras();
            String dirPosition = "/storage/emulated/0"; // or other values
            if (b != null) {
                if (b.getString("dir") != null) {
                    dirPosition = b.getString("dir");
                } else if (b.getString("dirBack2") != null) {
                    dirPosition = b.getString("dirBack2");
                }
            }
            final String rootPath = dirPosition;
            final File dir = new File(rootPath);
            files = dir.listFiles();
            final TextView pathOutput = findViewById(R.id.pathOutput);
            pathOutput.setText(rootPath);
            filesFountCount = files.length;
            final ListView listView = findViewById(R.id.listView);
            final TextAdapter textAdapter1 = new TextAdapter();
            listView.setAdapter(textAdapter1);

            filesList = new ArrayList<>();

            for (int i = 0; i < filesFountCount; i++) {
                filesList.add(String.valueOf(files[i].getAbsolutePath()));
            }
            textAdapter1.setData(filesList);


            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    selection[position] = !selection[position];
                    textAdapter1.setSelection(selection);
                    boolean isAtLeastOneSelected = false;
                    for (boolean aSelection : selection) {
                        if (aSelection) {
                            isAtLeastOneSelected = true;
                            break;
                        }
                    }
                    if (isAtLeastOneSelected) {
                        findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
                        lClickVerifier = true;
                    } else {
                        findViewById(R.id.bottomBar).setVisibility(View.GONE);
                        lClickVerifier = false;
                    }
                    return true;
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if (!lClickVerifier) {
                        if (files[position].isDirectory()) {
                            startActivity(new Intent(getBaseContext(), MainActivity.class));
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            Bundle b = new Bundle();
                            b.putString("dir", filesList.get(position));
                            b.putString("dirBack", rootPath);
                            intent.putExtras(b);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri uri = Uri.parse(files[position].toString());
                            if (files[position].toString().contains(".jpg") || files[position].toString().contains(".png") || files[position].toString().contains(".jpeg")) {
                                intent.setDataAndType(uri, "image/jpeg");
                            } else if (files[position].toString().contains(".mp4")) {
                                intent.setDataAndType(uri, "video/*");
                            } else {
                                intent.setDataAndType(uri, "*/*");
                            }
                            startActivity(intent);
                        }
                    } else {
                        selection[position] = !selection[position];
                        textAdapter1.setSelection(selection);
                        boolean isAtLeastOneSelected = false;
                        for (boolean aSelection : selection) {
                            if (aSelection) {
                                isAtLeastOneSelected = true;
                                break;
                            }
                        }
                        if (isAtLeastOneSelected) {
                            findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
                            lClickVerifier = true;
                        } else {
                            findViewById(R.id.bottomBar).setVisibility(View.GONE);
                            lClickVerifier = false;
                        }
                    }
                }
            });


            selection = new boolean[files.length];

            final Button bt1 = findViewById(R.id.bt1);
            final Button bt2 = findViewById(R.id.bt2);
            final Button bt3 = findViewById(R.id.bt3);
            final Button bt4 = findViewById(R.id.bt4);
            final Button bt5 = findViewById(R.id.bt5);

            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this);
                    deleteDialog.setTitle("Deletar");
                    deleteDialog.setMessage("Você tem certeza de que quer deletar esse(s) arquivo(s)?");
                    deleteDialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            for (int i = 0; i < files.length; i++) {
                                if (selection[i]) {

                                    deleteFileOrFolder(files[i]);
                                    selection[i] = false;
                                    boolean[] boolReset = new boolean[files.length];
                                    textAdapter1.setSelection(boolReset);

                                }
                            }
                            files = dir.listFiles();
                            filesFountCount = files.length;
                            filesList.clear();
                            for (int i = 0; i < filesFountCount; i++) {
                                filesList.add(String.valueOf(files[i].getAbsolutePath()));
                            }
                            textAdapter1.setData(filesList);
                            selection = new boolean[files.length];
                            findViewById(R.id.bottomBar).setVisibility(View.GONE);
                        }
                    });
                    deleteDialog.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
                    deleteDialog.show();
                }
            });

            bt2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //copiar arquivos
                }
            });

            isFileManagerInitialized = true;

            bt5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getBaseContext(), About.class));
                    finish();
                }
            });
        }
    }

    private void deleteFileOrFolder(File fileOrFolder) {
        if (fileOrFolder.isDirectory()) {
            if (fileOrFolder.list().length == 0) {
                fileOrFolder.delete();
            } else {
                String files[] = fileOrFolder.list();
                for (String temp : files) {
                    File fileToDelete = new File(fileOrFolder, temp);
                    deleteFileOrFolder(fileToDelete);
                }
                if (fileOrFolder.list().length == 0) {
                    fileOrFolder.delete();
                }
            }
        } else {
            fileOrFolder.delete();
        }
    }


    private void CopyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);  // Transferindo bytes de entrada para saída
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,
                                           final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0) {
            if (arePermissionsDenied()) {
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            } else {
                onResume();
            }
        }
    }

}