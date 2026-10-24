package com.digitaldeaddrop.app;
import android.Manifest;
import android.app.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import java.io.File;
import android.location.*;
import android.os.*;
import android.widget.*;
import org.json.*;
public final class MainActivity extends Activity {
  private JSONArray notes=new JSONArray(); private LinearLayout root; private Location point; private LocationManager gps; private String photo; private String pending;
  @Override public void onCreate(Bundle b){super.onCreate(b);gps=(LocationManager)getSystemService(LOCATION_SERVICE);try{notes=new JSONArray(getPreferences(0).getString("notes","[]"));}catch(Exception ignored){}home();}
  private void page(String heading){ScrollView s=new ScrollView(this);s.setBackgroundColor(Color.rgb(10,20,28));root=new LinearLayout(this);root.setPadding(30,30,30,30);root.setOrientation(LinearLayout.VERTICAL);s.addView(root);setContentView(s);label(heading,26);}
  private void label(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(0xffc2f7e5);t.setPadding(0,12,0,12);root.addView(t);}
  private void btn(String name,Runnable run){Button b=new Button(this);b.setText(name);root.addView(b);b.setOnClickListener(v->run.run());}
  private void alert(String m){new AlertDialog.Builder(this).setMessage(m).setPositiveButton("OK",null).show();}
  private void save(){getPreferences(0).edit().putString("notes",notes.toString()).apply();}
  private void locate(Runnable next){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10);alert("Grant precise location, then retry.");return;}if(!gps.isProviderEnabled(LocationManager.GPS_PROVIDER)){alert("Enable GPS and retry.");return;}label("Getting GPS fix…",13);try{gps.getCurrentLocation(LocationManager.GPS_PROVIDER,new CancellationSignal(),getMainExecutor(),p->{if(p==null||p.hasAccuracy()&&p.getAccuracy()>100){alert("GPS fix unavailable or accuracy worse than 100 metres; retry outdoors.");return;}point=p;next.run();});}catch(Exception e){alert("GPS unavailable.");}}
  private void photoCapture(){
    try{
      File dir=new File(getFilesDir(),"note_images");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException();
      File file=File.createTempFile("note_"+System.currentTimeMillis()+"_",".jpg",dir);
      pending=file.getName();
      Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
      Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      camera.putExtra(MediaStore.EXTRA_OUTPUT,uri);
      camera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
      startActivityForResult(camera,42);
    }catch(Exception e){pending=null;alert("Camera unavailable.");}
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(request!=42)return;
    if(result==RESULT_OK && pending!=null){
      File file=new File(new File(getFilesDir(),"note_images"),pending);
      if(file.length()>0){photo=pending;alert("Photo captured. Save note to keep it.");}
      else alert("Photo was not saved.");
    }else if(pending!=null)removePhoto(pending);
    pending=null;
  }
  private void removePhoto(String name){
    if(name!=null&&name.matches("note_[0-9]+_[0-9]+[.]jpg"))
      new File(new File(getFilesDir(),"note_images"),name).delete();
  }
  private void displayPhoto(String name){
    if(name==null||!name.matches("note_[0-9]+_[0-9]+[.]jpg"))return;
    File f=new File(new File(getFilesDir(),"note_images"),name);
    if(f.isFile()){ImageView img=new ImageView(this);img.setAdjustViewBounds(true);img.setMaxHeight(700);img.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));root.addView(img);}
  }
  private void openNote(JSONObject n){
    try{n.put("localOpens",n.optInt("localOpens",0)+1);save();}catch(Exception ignored){}
    page("NOTE");label(n.optString("text"),17);displayPhoto(n.optString("photo",null));
    label("Local opens: "+n.optInt("localOpens",0),14);
    label("Remote read receipts unavailable until shared backend is connected.",13);btn("BACK",this::home);
  }
  private void home(){page("DIGITAL DEAD DROP");label("Local offline prototype. Notes remain on this phone only, not on other devices.",15);btn("DROP NOTE",()->locate(this::create));btn("DISCOVER NEARBY",()->locate(this::nearby));btn("MANAGE NOTES",this::manage);label("GPS position can be spoofed. Not suitable for secure or anonymous evidence handling.",13);}
  private void create(){photo=null;page("DROP NOTE");label("At "+point.getLatitude()+", "+point.getLongitude(),13);EditText message=new EditText(this);message.setHint("Enter note");message.setTextColor(-1);root.addView(message);EditText radius=new EditText(this);radius.setText("25");radius.setTextColor(-1);radius.setInputType(2);root.addView(radius);label("Radius in metres, 5 to 100",13);btn("TAKE PHOTO",this::photoCapture);btn("SAVE",()->{String m=message.getText().toString().trim();int r;try{r=Integer.parseInt(radius.getText().toString());}catch(Exception e){alert("Invalid radius");return;}if((m.length()<1&&photo==null)||m.length()>2000||r<5||r>100){alert("Invalid note or radius");return;}try{JSONObject n=new JSONObject();n.put("text",m);n.put("lat",point.getLatitude());n.put("lon",point.getLongitude());n.put("r",r);if(photo!=null)n.put("photo",photo);n.put("localOpens",0);notes.put(n);save();photo=null;home();}catch(Exception e){alert("Save failed");}});btn("BACK",()->{removePhoto(photo);photo=null;home();});}
  private void nearby(){page("NEARBY NOTES");int found=0;for(int i=0;i<notes.length();i++){JSONObject n=notes.optJSONObject(i);if(n==null)continue;float[] dist=new float[1];Location.distanceBetween(point.getLatitude(),point.getLongitude(),n.optDouble("lat"),n.optDouble("lon"),dist);if(dist[0]<=n.optInt("r")){final JSONObject selected=n;btn("OPEN NOTE "+(i+1)+" ("+Math.round(dist[0])+" m)",()->openNote(selected));found++;}}if(found==0)label("No notes within their GPS radius.",16);btn("REFRESH",()->locate(this::nearby));btn("BACK",this::home);}
  private void manage(){page("LOCAL NOTES");for(int i=0;i<notes.length();i++){final int item=i;JSONObject n=notes.optJSONObject(i);if(n==null)continue;label(n.optString("text")+" | Local opens: "+n.optInt("localOpens",0)+" | Remote receipts unavailable",16);btn("DELETE NOTE "+(i+1),()->new AlertDialog.Builder(this).setMessage("Delete note?").setNegativeButton("CANCEL",null).setPositiveButton("DELETE",(d,w)->{JSONArray a=new JSONArray();for(int j=0;j<notes.length();j++)if(j!=item)a.put(notes.optJSONObject(j));removePhoto(n.optString("photo",null));notes=a;save();manage();}).show());}btn("BACK",this::home);}
}