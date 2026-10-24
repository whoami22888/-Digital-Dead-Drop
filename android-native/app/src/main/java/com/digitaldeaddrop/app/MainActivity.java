package com.digitaldeaddrop.app;
import android.Manifest;
import android.app.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.*;
import android.os.*;
import android.widget.*;
import org.json.*;
public final class MainActivity extends Activity {
  private JSONArray notes=new JSONArray(); private LinearLayout root; private Location point; private LocationManager gps;
  @Override public void onCreate(Bundle b){super.onCreate(b);gps=(LocationManager)getSystemService(LOCATION_SERVICE);try{notes=new JSONArray(getPreferences(0).getString("notes","[]"));}catch(Exception ignored){}home();}
  private void page(String heading){ScrollView s=new ScrollView(this);s.setBackgroundColor(Color.rgb(10,20,28));root=new LinearLayout(this);root.setPadding(30,30,30,30);root.setOrientation(1);s.addView(root);setContentView(s);label(heading,26);}
  private void label(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(0xffc2f7e5);t.setPadding(0,12,0,12);root.addView(t);}
  private void btn(String name,Runnable run){Button b=new Button(this);b.setText(name);root.addView(b);b.setOnClickListener(v->run.run());}
  private void alert(String m){new AlertDialog.Builder(this).setMessage(m).setPositiveButton("OK",null).show();}
  private void save(){getPreferences(0).edit().putString("notes",notes.toString()).apply();}
  private void locate(Runnable next){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10);alert("Grant precise location, then retry.");return;}if(!gps.isProviderEnabled(LocationManager.GPS_PROVIDER)){alert("Enable GPS and retry.");return;}label("Getting GPS fix…",13);try{gps.getCurrentLocation(LocationManager.GPS_PROVIDER,new CancellationSignal(),getMainExecutor(),p->{if(p==null||p.hasAccuracy()&&p.getAccuracy()>100){alert("GPS fix unavailable or accuracy worse than 100 metres; retry outdoors.");return;}point=p;next.run();});}catch(Exception e){alert("GPS unavailable.");}}
  private void home(){page("DIGITAL DEAD DROP");label("Local offline prototype. Notes remain on this phone only, not on other devices.",15);btn("DROP NOTE",()->locate(this::create));btn("DISCOVER NEARBY",()->locate(this::nearby));btn("MANAGE NOTES",this::manage);label("GPS position can be spoofed. Not suitable for secure or anonymous evidence handling.",13);}
  private void create(){page("DROP NOTE");label("At "+point.getLatitude()+", "+point.getLongitude(),13);EditText message=new EditText(this);message.setHint("Enter note");message.setTextColor(-1);root.addView(message);EditText radius=new EditText(this);radius.setText("100");radius.setTextColor(-1);radius.setInputType(2);root.addView(radius);label("Radius in metres, 25 to 1000",13);btn("SAVE",()->{String m=message.getText().toString().trim();int r;try{r=Integer.parseInt(radius.getText().toString());}catch(Exception e){alert("Invalid radius");return;}if(m.length()<1||m.length()>2000||r<25||r>1000){alert("Invalid note or radius");return;}try{JSONObject n=new JSONObject();n.put("text",m);n.put("lat",point.getLatitude());n.put("lon",point.getLongitude());n.put("r",r);notes.put(n);save();home();}catch(Exception e){alert("Save failed");}});btn("BACK",this::home);}
  private void nearby(){page("NEARBY NOTES");int found=0;for(int i=0;i<notes.length();i++){JSONObject n=notes.optJSONObject(i);if(n==null)continue;float[] dist=new float[1];Location.distanceBetween(point.getLatitude(),point.getLongitude(),n.optDouble("lat"),n.optDouble("lon"),dist);if(dist[0]<=n.optInt("r")){label(n.optString("text")+" ("+Math.round(dist[0])+" m away)",17);found++;}}if(found==0)label("No notes within their GPS radius.",16);btn("REFRESH",()->locate(this::nearby));btn("BACK",this::home);}
  private void manage(){page("LOCAL NOTES");for(int i=0;i<notes.length();i++){final int item=i;JSONObject n=notes.optJSONObject(i);if(n==null)continue;label(n.optString("text"),16);btn("DELETE NOTE "+(i+1),()->new AlertDialog.Builder(this).setMessage("Delete note?").setNegativeButton("CANCEL",null).setPositiveButton("DELETE",(d,w)->{JSONArray a=new JSONArray();for(int j=0;j<notes.length();j++)if(j!=item)a.put(notes.optJSONObject(j));notes=a;save();manage();}).show());}btn("BACK",this::home);}
}