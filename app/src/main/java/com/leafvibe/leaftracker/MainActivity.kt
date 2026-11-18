package com.leafvibe.leaftracker

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.leafvibe.leaftracker.data.SecureStore
import com.leafvibe.leaftracker.security.PinManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.time.LocalDate
import java.time.DayOfWeek

class MainActivity : Activity() {
    private lateinit var pin: PinManager
    private lateinit var store: SecureStore
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private var data = JSONObject()
    private var tab = "Identités"
    private var space = "Tous"
    private var unlocked = false
    private var relock = false
    private var vaultReady = false
    private var pendingPhoto = ""
    private var pendingPhotoView: ImageView? = null
    private var calendarMode = "Mois"
    private var monthOffset = 0
    private var swipeStartX = 0f
    private var swipeStartY = 0f
    private val green = 0xFF236B4B.toInt()
    private val ink = 0xFF183329.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pin = PinManager(this); store = SecureStore(this)
        if (pin.isConfigured()) unlockScreen() else setupPin()
    }
    override fun dispatchTouchEvent(event:MotionEvent):Boolean {
        if(event.action==MotionEvent.ACTION_DOWN){swipeStartX=event.x;swipeStartY=event.y}
        val result=super.dispatchTouchEvent(event)
        if(event.action==MotionEvent.ACTION_UP && unlocked) {
            val dx=event.x-swipeStartX; val dy=event.y-swipeStartY
            if(kotlin.math.abs(dx)>125f && kotlin.math.abs(dx)>kotlin.math.abs(dy)*1.35f) {
                val next=if(dx<0) "Modules" else "Calendrier"
                if(tab!=next){tab=next;render()}
            }
        }
        return result
    }
    override fun onStop() { super.onStop(); if (unlocked) { unlocked = false; relock = true } }
    override fun onResume() { super.onResume(); if (relock && pin.isConfigured()) { relock = false; unlockScreen() } }

    private fun frame(title: String): LinearLayout {
        val scroll = ScrollView(this)
        root = LinearLayout(this).apply { orientation = 1; setPadding(18, 22, 18, 18); setBackgroundColor(0xFFF4F7F4.toInt()) }
        scroll.addView(root)
        root.addView(TextView(this).apply { text = title; textSize = 27f; setTextColor(ink); setTypeface(null, 1) })
        setContentView(scroll)
        return root
    }
    private fun setupPin() {
        val l = frame("Bienvenue sur LeafTracker")
        text(l, "Crée un PIN obligatoire pour protéger ton coffre local chiffré.")
        val a = pinField("PIN (4 à 8 chiffres)"); val b = pinField("Confirmer le PIN")
        l.addView(a); l.addView(b)
        button(l, "Créer mon PIN") {
            val x = a.text.toString(); if (x.length !in 4..8 || x.any { !it.isDigit() }) toast("PIN de 4 à 8 chiffres requis")
            else if (x != b.text.toString()) toast("Les PIN ne correspondent pas")
            else { pin.setPin(x.toCharArray()); openVault() }
        }
    }
    private fun unlockScreen() {
        val l = frame("LeafTracker verrouillé"); text(l, "Saisis ton code PIN.")
        val field = pinField("Code PIN"); l.addView(field)
        button(l, "Déverrouiller") {
            if (pin.isLocked()) toast("Attends ${pin.remainingLockMillis()/1000+1}s")
            else if (pin.verify(field.text.toString().toCharArray())) { pin.registerSuccess(); openVault() }
            else { pin.registerFailure(); field.text.clear(); toast("PIN incorrect") }
        }
    }
    private fun openVault() {
        unlocked = true
        try { data = store.read(); vaultReady = true; updateWidgetCache(); render() }
        catch (_: Exception) { vaultReady = false; val l = frame("Coffre indisponible"); text(l, "Déchiffrement impossible. Aucune donnée vide ne sera écrite par-dessus le coffre existant. Ne désinstalle pas l'application et n'efface pas ses données.") }
    }
    override fun onActivityResult(requestCode:Int,resultCode:Int,dataIntent:Intent?) {
        super.onActivityResult(requestCode,resultCode,dataIntent)
        if(requestCode==701 && resultCode==Activity.RESULT_OK) {
            val uri=dataIntent?.data ?: return
            try { contentResolver.openInputStream(uri)?.use { stream ->
                val bmp=BitmapFactory.decodeStream(stream) ?: return
                val width=minOf(640,bmp.width)
                val scaled=Bitmap.createScaledBitmap(bmp,width,(bmp.height.toFloat()*width/bmp.width).toInt().coerceAtLeast(1),true)
                val out=java.io.ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG,78,out)
                pendingPhoto=Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP)
                pendingPhotoView?.setImageBitmap(scaled)
            }; toast("Photo ajoutée à la fiche") } catch(_:Exception) { toast("Impossible de lire cette image") }
        }
    }
    private fun render() {
        val l = frame("LeafTracker")
        val bar = LinearLayout(this).apply { orientation = 0; gravity = Gravity.CENTER_VERTICAL }
        val selector = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Tous", "Privé", "Professionnel"))
            setSelection(listOf("Tous", "Privé", "Professionnel").indexOf(space).coerceAtLeast(0))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p: AdapterView<*>?) {}
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { val s=listOf("Tous","Privé","Professionnel")[pos]; if (s!=space) { space=s; render() } }
            }
        }
        bar.addView(selector, LinearLayout.LayoutParams(0,-2,1f))
        bar.addView(Button(this).apply { text="Verrouiller"; isAllCaps=false; setOnClickListener { unlocked=false; unlockScreen() } })
        l.addView(bar)
        val nav = LinearLayout(this).apply { orientation=0; gravity=Gravity.CENTER }
        nav.addView(Button(this).apply { text="‹ Leaf modules"; isAllCaps=false; setOnClickListener { tab="Modules"; render() } },LinearLayout.LayoutParams(0,-2,1f))
        nav.addView(Button(this).apply { text="Identités"; isAllCaps=false; setOnClickListener { tab="Identités"; render() } },LinearLayout.LayoutParams(0,-2,1f))
        nav.addView(Button(this).apply { text="LeafCalendar ›"; isAllCaps=false; setOnClickListener { tab="Calendrier"; render() } },LinearLayout.LayoutParams(0,-2,1f))
        l.addView(nav)
        content = LinearLayout(this).apply { orientation=1; setPadding(0,12,0,0) }; l.addView(content)
        when(tab) { "Identités" -> identitiesUI(); "Social" -> socialUI(); "Graph" -> graphUI(); "Calendrier" -> calendarUI(); "Modules" -> modulesUI(); else -> notesUI() }
        text(l, "LEAFVIBE • Hors ligne • Coffre AES-GCM", 11)
    }
    private fun arr(k:String)=data.optJSONArray(k) ?: JSONArray()
    private fun shown(o:JSONObject)=space=="Tous" || o.optString("workspace","Privé")==space
    private fun modulesUI() {
        text(content,"LeafVibe · Modules",22,true)
        text(content,"Les espaces de ton univers LeafTracker.")
        val modules=listOf("LeafIdentity" to "Profils détaillés et fiches personnalisées","LeafSocial" to "Famille, amis, collègues et relations","LeafGraph" to "Cartographie des personnes et des liens","LeafCalendar" to "Calendriers personnels et professionnels","LeafNotes" to "Notes, mémos et recherche","LeafGoals" to "Objectifs, projets et ambitions","LeafMoments" to "Souvenirs et dates importantes","LeafPlaces" to "Adresses et lieux utiles","LeafTasks" to "Tâches et listes de suivi","LeafArchive" to "Éléments archivés")
        modules.forEach{(name,desc)->card("$name\n$desc"){tab=when(name){"LeafIdentity"->"Identités";"LeafSocial"->"Social";"LeafGraph"->"Graph";"LeafCalendar"->"Calendrier";else->"Notes"};render()}}
    }
    private fun identitiesUI() {
        text(content,"LeafIdentity · Carnet des identités",21,true)
        text(content,"Des fiches complètes pour la famille, les amis, l'entourage et les collègues.")
        button(content,"＋ Créer une identité"){ identityDialog() }
        var count=0
        for(i in 0 until arr("identities").length()) {
            val x=arr("identities").getJSONObject(i); if(!shown(x)) continue; count++
            val line=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER_VERTICAL;setPadding(10,10,10,10);setBackgroundColor(0xFFE5EFE8.toInt())}
            val photo=x.optString("photo")
            if(photo.isNotBlank()) try { val bytes=Base64.decode(photo,Base64.DEFAULT); val bmp=BitmapFactory.decodeByteArray(bytes,0,bytes.size); if(bmp!=null) line.addView(ImageView(this).apply{setImageBitmap(bmp);scaleType=ImageView.ScaleType.CENTER_CROP},LinearLayout.LayoutParams(72,72).apply{rightMargin=10}) } catch(_:Exception){}
            val info=LinearLayout(this).apply{orientation=1}
            text(info,x.optString("firstName")+" "+x.optString("name"),17,true)
            text(info,x.optString("category")+" · "+x.optString("workspace")+"\n"+x.optString("jobTitle"))
            line.addView(info,LinearLayout.LayoutParams(0,-2,1f)); line.setOnClickListener{identityActions(x)}
            content.addView(line,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=8})
        }
        if(count==0) text(content,"Aucune identité. Crée ta première fiche pour commencer.")
    }
    private fun identityDialog(old:JSONObject?=null) {
        pendingPhoto=old?.optString("photo","") ?: ""
        val photoView=ImageView(this).apply{layoutParams=LinearLayout.LayoutParams(112,112).apply{gravity=Gravity.CENTER};scaleType=ImageView.ScaleType.CENTER_CROP;setBackgroundColor(0xFFDCE8DF.toInt())}
        if(pendingPhoto.isNotBlank()) try { val b=Base64.decode(pendingPhoto,Base64.DEFAULT); photoView.setImageBitmap(BitmapFactory.decodeByteArray(b,0,b.size)) } catch(_:Exception){}
        pendingPhotoView=photoView
        val photoButton=Button(this).apply{text="Ajouter / changer la photo (JPG, PNG…)";isAllCaps=false;setOnClickListener{startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="image/*"},701)}}
        fun field(key:String,hint:String)=input(hint).apply{setText(old?.optString(key,"") ?: "")}
        val last=field("name","Nom"); val first=field("firstName","Prénom")
        val age=field("age","Âge"); val birthday=field("birthday","Date d'anniversaire (JJ/MM/AAAA)")
        val home=field("homeAddress","Adresse personnelle"); val job=field("jobTitle","Métier / fonction"); val work=field("workAddress","Adresse du lieu de travail")
        val category=field("category","Catégorie : famille, ami, collègue…")
        val personality=field("personality","Personnalité, caractère, tempérament")
        val emotions=field("emotions","Émotions, sensibilité, humeur habituelle")
        val interests=field("interests","Goûts, passions, centres d'intérêt")
        val habits=field("habits","Habitudes et préférences")
        val goals=field("goal","Objectifs, projets et ambitions")
        val description=field("description","Autres informations / histoire")
        val privateBox=CheckBox(this).apply{text="Privé";isChecked=old?.optString("workspace","Privé")!="Professionnel"}
        val proBox=CheckBox(this).apply{text="Professionnel";isChecked=old?.optString("workspace")=="Professionnel"}
        privateBox.setOnCheckedChangeListener{_,v->if(v)proBox.isChecked=false else if(!proBox.isChecked)privateBox.isChecked=true}
        proBox.setOnCheckedChangeListener{_,v->if(v)privateBox.isChecked=false else if(!privateBox.isChecked)proBox.isChecked=true}
        val form=LinearLayout(this).apply{orientation=1;setPadding(22,12,22,12)}
        text(form,"PHOTO DE PROFIL",12,true);form.addView(photoView);form.addView(photoButton)
        text(form,"IDENTITÉ",13,true);form.addView(last);form.addView(first);form.addView(age);form.addView(birthday)
        text(form,"ESPACE",13,true);form.addView(privateBox);form.addView(proBox)
        text(form,"COORDONNÉES",13,true);form.addView(home);form.addView(job);form.addView(work)
        text(form,"PERSONNALITÉ ET VIE PERSONNELLE",13,true);form.addView(personality);form.addView(emotions);form.addView(interests);form.addView(habits);form.addView(goals);form.addView(description);form.addView(category)
        val scroll=ScrollView(this).apply{addView(form)}
        AlertDialog.Builder(this).setTitle(if(old==null)"Créer une identité" else "Modifier l'identité").setView(scroll).setNegativeButton("Annuler",null).setPositiveButton(if(old==null)"Créer la fiche" else "Enregistrer"){_,_->
            if(last.text.isNullOrBlank()||first.text.isNullOrBlank()) toast("Le nom et le prénom sont obligatoires") else {
                val x=old?:JSONObject().put("id",UUID.randomUUID().toString())
                x.put("name",last.text.toString()).put("firstName",first.text.toString()).put("age",age.text.toString()).put("birthday",birthday.text.toString()).put("homeAddress",home.text.toString()).put("jobTitle",job.text.toString()).put("workAddress",work.text.toString()).put("personality",personality.text.toString()).put("emotions",emotions.text.toString()).put("interests",interests.text.toString()).put("habits",habits.text.toString()).put("goal",goals.text.toString()).put("description",description.text.toString()).put("category",category.text.toString()).put("workspace",if(proBox.isChecked)"Professionnel" else "Privé").put("photo",pendingPhoto)
                if(old==null)arr("identities").put(x);data.put("identities",arr("identities"));save();render()
            }
        }.show()
    }
    private fun identityActions(x:JSONObject) {
        AlertDialog.Builder(this).setTitle(x.optString("firstName")+" "+x.optString("name")).setItems(arrayOf("Consulter la fiche","Modifier","Dupliquer","Supprimer")){_,i->when(i){
            0->AlertDialog.Builder(this).setTitle(x.optString("firstName")+" "+x.optString("name")).setMessage("Âge : "+x.optString("age")+"\nAnniversaire : "+x.optString("birthday")+"\nEspace : "+x.optString("workspace")+"\nAdresse : "+x.optString("homeAddress")+"\nMétier : "+x.optString("jobTitle")+"\nTravail : "+x.optString("workAddress")+"\n\nPersonnalité : "+x.optString("personality")+"\nÉmotions : "+x.optString("emotions")+"\nGoûts : "+x.optString("interests")+"\nHabitudes : "+x.optString("habits")+"\nObjectifs : "+x.optString("goal")+"\n\n"+x.optString("description")).setPositiveButton("Fermer",null).setNeutralButton("Modifier"){_,_->identityDialog(x)}.show()
            1->identityDialog(x)
            2->{val c=JSONObject(x.toString()).put("id",UUID.randomUUID().toString()).put("name",x.optString("name")+" (copie)");arr("identities").put(c);data.put("identities",arr("identities"));save();render()}
            3->AlertDialog.Builder(this).setMessage("Supprimer cette identité ?").setNegativeButton("Annuler",null).setPositiveButton("Supprimer"){_,_->remove("identities",x.optString("id"));render()}.show()
        }}.show()
    }
    private fun socialUI() {
        text(content,"LeafSocial · Relations",21,true); button(content,"+ Créer une relation"){ relationshipDialog() }
        var n=0
        for(i in 0 until arr("relationships").length()) { val r=arr("relationships").getJSONObject(i); if(space!="Tous"&&r.optString("workspace")!=space) continue
            val a=findName(r.optString("from")); val b=findName(r.optString("to")); card("$a — ${r.optString("type")} → $b\n${r.optString("workspace")} · ${r.optString("notes")}") { AlertDialog.Builder(this).setMessage("Supprimer cette relation ?").setNegativeButton("Annuler",null).setPositiveButton("Supprimer"){_,_->remove("relationships",r.optString("id"));render()}.show() }; n++
        }
        if(n==0) text(content,"Aucune relation enregistrée.")
    }
    private fun relationshipDialog() {
        if(arr("identities").length()<2){toast("Crée au moins deux identités");return}
        val ids=mutableListOf<String>(); val names=mutableListOf<String>()
        for(i in 0 until arr("identities").length()){val x=arr("identities").getJSONObject(i);ids.add(x.optString("id"));names.add(x.optString("name"))}
        val a=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,names)}
        val b=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,names);setSelection(1)}
        val type=input("Type : parent, ami, collègue…"); val note=input("Description"); val ws=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Privé","Professionnel"))}
        AlertDialog.Builder(this).setTitle("Nouvelle relation").setView(column(type,note,a,b,ws)).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer"){_,_->
            if(a.selectedItemPosition==b.selectedItemPosition) toast("Choisis deux personnes différentes") else {arr("relationships").put(JSONObject().put("id",UUID.randomUUID().toString()).put("from",ids[a.selectedItemPosition]).put("to",ids[b.selectedItemPosition]).put("type",type.text.toString()).put("notes",note.text.toString()).put("workspace",if(ws.selectedItemPosition==1)"Professionnel" else "Privé").put("widgetVisible",widgetVisible.isChecked));data.put("relationships",arr("relationships"));save();render()}
        }.show()
    }
    private fun graphUI() {
        text(content,"LeafGraph · Réseau",21,true); text(content,"Vue initiale des personnes et des liens enregistrés. L'éditeur graphique libre sera développé ensuite.")
        for(i in 0 until arr("identities").length()){val x=arr("identities").getJSONObject(i);if(shown(x))text(content,"● ${x.optString("name")}")}
        for(i in 0 until arr("relationships").length()){val r=arr("relationships").getJSONObject(i);if(space=="Tous"||r.optString("workspace")==space)text(content,"${findName(r.optString("from"))} — ${r.optString("type")} — ${findName(r.optString("to"))}")}
    }
    private fun calendarUI() {
        text(content,"LeafCalendar",22,true)
        text(content,"Calendriers personnels, familiaux et professionnels réunis dans une seule vue locale.")
        val modes=LinearLayout(this).apply{orientation=0}
        listOf("Mois","Semaine","Jour","Liste").forEach{mode->
            modes.addView(Button(this).apply{text=mode;textSize=11f;isAllCaps=false;setOnClickListener{calendarMode=mode;render()}},LinearLayout.LayoutParams(0,-2,1f))
        }
        content.addView(modes)
        val today=LocalDate.now()
        val month=today.withDayOfMonth(1).plusMonths(monthOffset.toLong())
        val period=LinearLayout(this).apply{orientation=0;gravity=Gravity.CENTER_VERTICAL}
        period.addView(Button(this).apply{text="‹";isAllCaps=false;setOnClickListener{monthOffset--;render()}})
        period.addView(TextView(this).apply{text=month.month.name+" "+month.year;textSize=16f;setTextColor(ink);gravity=Gravity.CENTER},LinearLayout.LayoutParams(0,-2,1f))
        period.addView(Button(this).apply{text="›";isAllCaps=false;setOnClickListener{monthOffset++;render()}})
        content.addView(period)
        button(content,"＋ Nouvel événement"){eventDialog()}
        button(content,"＋ Mémo / liste"){calendarMemoDialog()}
        val events=arr("events")
        val all=(0 until events.length()).map{events.getJSONObject(it)}.filter{shown(it)}
        val startWeek=today.with(DayOfWeek.MONDAY)
        val endWeek=startWeek.plusDays(6)
        val visible=all.filter{e->
            val date=try{LocalDate.parse(e.optString("date"))}catch(_:Exception){null}
            when(calendarMode){
                "Jour"->date==today
                "Semaine"->date!=null&&!date.isBefore(startWeek)&&!date.isAfter(endWeek)
                "Mois"->date!=null&&date.year==month.year&&date.month==month.month
                else->true
            }
        }.sortedBy{it.optString("date")+" "+it.optString("time")}
        text(content,when(calendarMode){"Jour"->"Aujourd'hui";"Semaine"->"Cette semaine";"Mois"->"Événements du mois";else->"Tous les événements"},15,true)
        if(visible.isEmpty())text(content,"Aucun événement dans cette période.")
        visible.forEach{e->
            val detail="📅 "+e.optString("title")+"\n"+e.optString("date")+" · "+e.optString("time")+"\n"+e.optString("calendar")+" · "+e.optString("workspace")+"\n"+e.optString("location")+"\n"+e.optString("notes")+"\nParticipants : "+e.optString("participants")+" · "+e.optString("recurrence")
            card(detail){AlertDialog.Builder(this).setTitle(e.optString("title")).setItems(arrayOf("Consulter / modifier","Supprimer")){_,n->if(n==0)eventDialog(e)else AlertDialog.Builder(this).setMessage("Supprimer cet événement ?").setNegativeButton("Annuler",null).setPositiveButton("Supprimer"){_,_->remove("events",e.optString("id"));render()}.show()}.show()}
        }
        text(content,"Mémos et listes",17,true)
        for(i in 0 until arr("calendarMemos").length()){val memo=arr("calendarMemos").getJSONObject(i);if(shown(memo))card(memo.optString("title")+"\n"+memo.optString("body")){AlertDialog.Builder(this).setMessage(memo.optString("body")).setPositiveButton("Fermer",null).setNeutralButton("Supprimer"){_,_->remove("calendarMemos",memo.optString("id"));render()}.show()}}
        text(content,"Les événements sélectionnés peuvent aussi apparaître sur le widget d'accueil.",11)
    }
    private fun calendarMemoDialog(){
        val title=input("Titre du mémo / de la liste");val body=input("Contenu ou éléments de la liste")
        AlertDialog.Builder(this).setTitle("Nouveau mémo").setView(column(title,body)).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer"){_,_->if(title.text.isNotBlank()){arr("calendarMemos").put(JSONObject().put("id",UUID.randomUUID().toString()).put("title",title.text.toString()).put("body",body.text.toString()).put("workspace",if(space=="Professionnel")"Professionnel" else "Privé"));data.put("calendarMemos",arr("calendarMemos"));save();render()}}.show()
    }
    private fun eventDialog(old:JSONObject?=null) {
        val title=input("Titre de l'événement");val date=input("Date AAAA-MM-JJ");val time=input("Heure HH:MM");val cal=input("Calendrier : Famille, Amis, Travail");val location=input("Lieu / adresse");val participants=input("Participants");val recurrence=input("Récurrence : aucune, quotidienne, hebdomadaire, mensuelle, annuelle");val note=input("Description / notes / commentaires")
        val widgetVisible=CheckBox(this).apply{text="Afficher sur le widget (visible hors de l’application)";isChecked=old?.optBoolean("widgetVisible",false)?:false}
        if(old!=null){title.setText(old.optString("title"));date.setText(old.optString("date"));time.setText(old.optString("time"));cal.setText(old.optString("calendar"));location.setText(old.optString("location"));participants.setText(old.optString("participants"));recurrence.setText(old.optString("recurrence"));note.setText(old.optString("notes"))}
        val ws=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Privé","Professionnel"));setSelection(if(old?.optString("workspace",space)=="Professionnel")1 else 0)}
        AlertDialog.Builder(this).setTitle(if(old==null)"Nouvel événement" else "Modifier l'événement").setView(ScrollView(this).apply{addView(column(title,date,time,cal,location,participants,recurrence,note,ws,widgetVisible))}).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer"){_,_->
            if(title.text.isNullOrBlank())toast("Titre obligatoire")else if(date.text.isNotBlank()&&!date.text.toString().matches(Regex("\\d{4}-\\d{2}-\\d{2}")))toast("Date au format AAAA-MM-JJ")else{
                val e=old?:JSONObject().put("id",UUID.randomUUID().toString())
                e.put("title",title.text.toString()).put("date",date.text.toString()).put("time",time.text.toString()).put("calendar",cal.text.toString()).put("location",location.text.toString()).put("participants",participants.text.toString()).put("recurrence",recurrence.text.toString()).put("notes",note.text.toString()).put("workspace",if(ws.selectedItemPosition==1)"Professionnel" else "Privé")
                if(old==null)arr("events").put(e);data.put("events",arr("events"));save();render()
            }
        }.show()
    }
    private fun notesUI() {
        text(content,"LeafNotes · Recherche",21,true);val q=input("Rechercher identités et notes")
        button(content,"Rechercher"){val term=q.text.toString().lowercase();val hits=mutableListOf<String>();for(i in 0 until arr("identities").length()){val x=arr("identities").getJSONObject(i);if(shown(x)&&(x.optString("name")+x.optString("description")).lowercase().contains(term))hits.add("Identité : ${x.optString("name")}")};for(i in 0 until arr("notes").length()){val x=arr("notes").getJSONObject(i);if((space=="Tous"||x.optString("workspace")==space)&&(x.optString("title")+x.optString("body")).lowercase().contains(term))hits.add("Note : ${x.optString("title")}")};AlertDialog.Builder(this).setTitle("Résultats : ${hits.size}").setMessage(hits.joinToString("\n")).setPositiveButton("Fermer",null).show()}
        button(content,"+ Nouvelle note"){val t=input("Titre");val b=input("Contenu");AlertDialog.Builder(this).setTitle("Nouvelle note").setView(column(t,b)).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer"){_,_->if(t.text.isNotBlank()){arr("notes").put(JSONObject().put("id",UUID.randomUUID().toString()).put("title",t.text.toString()).put("body",b.text.toString()).put("workspace",if(space=="Professionnel")"Professionnel" else "Privé"));data.put("notes",arr("notes"));save();render()}}.show()}
        for(i in 0 until arr("notes").length()){val n=arr("notes").getJSONObject(i);if(space=="Tous"||n.optString("workspace")==space)card("${n.optString("title")}\n${n.optString("body")}"){AlertDialog.Builder(this).setMessage(n.optString("body")).setPositiveButton("Fermer",null).setNeutralButton("Supprimer"){_,_->remove("notes",n.optString("id"));render()}.show()}}
    }
    private fun changePin() {
        val old=pinField("PIN actuel");val next=pinField("Nouveau PIN");val confirm=pinField("Confirmer")
        AlertDialog.Builder(this).setTitle("Changer le PIN").setView(column(old,next,confirm)).setNegativeButton("Annuler",null).setPositiveButton("Valider"){_,_->val p=next.text.toString();if(!pin.verify(old.text.toString().toCharArray()))toast("PIN actuel incorrect")else if(p.length !in 4..8||p.any{!it.isDigit()})toast("PIN de 4 à 8 chiffres")else if(p!=confirm.text.toString())toast("Confirmation incorrecte")else{pin.setPin(p.toCharArray());toast("PIN modifié")}}.show()
    }
    private fun findName(id:String):String {for(i in 0 until arr("identities").length()){val x=arr("identities").getJSONObject(i);if(x.optString("id")==id)return x.optString("name")};return "Identité supprimée"}
    private fun remove(key:String,id:String){val next=JSONArray();for(i in 0 until arr(key).length()){val x=arr(key).getJSONObject(i);if(x.optString("id")!=id)next.put(x)};data.put(key,next);save()}
    private fun save(){if(!vaultReady){toast("Coffre indisponible : écriture bloquée");return};try{store.write(data);updateWidgetCache()}catch(_:Exception){toast("Erreur de sauvegarde chiffrée")}}
    private fun updateWidgetCache(){
        val items=JSONArray()
        val all=arr("events")
        val selected=(0 until all.length()).map{all.getJSONObject(it)}.filter{it.optBoolean("widgetVisible",false)&&it.optString("date").isNotBlank()}.sortedBy{it.optString("date")+" "+it.optString("time")}
        selected.take(5).forEach{e->items.put(JSONObject().put("title",e.optString("title")).put("date",e.optString("date")).put("time",e.optString("time")))}
        getSharedPreferences("leaftracker_widget",MODE_PRIVATE).edit().putString("events",items.toString()).apply()
        sendBroadcast(Intent(this,LeafTrackerWidgetProvider::class.java).setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE))
    }
    private fun column(vararg v:View)=LinearLayout(this).apply{orientation=1;setPadding(22,8,22,0);v.forEach{addView(it)}}
    private fun input(h:String)=EditText(this).apply{hint=h;textSize=15f}
    private fun pinField(h:String)=EditText(this).apply{hint=h;inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;textSize=19f}
    private fun text(parent:LinearLayout,s:String,size:Number=15f,bold:Boolean=false){parent.addView(TextView(this).apply{text=s;textSize=size.toFloat();setTextColor(ink);if(bold)setTypeface(null,1);setPadding(0,8,0,8)})}
    private fun button(parent:LinearLayout,s:String,action:()->Unit){parent.addView(Button(this).apply{text=s;isAllCaps=false;setTextColor(green);setOnClickListener{action()}})}
    private fun card(s:String,action:()->Unit){content.addView(TextView(this).apply{text=s;textSize=15f;setTextColor(ink);setPadding(13,13,13,13);setBackgroundColor(0xFFE5EFE8.toInt());setOnClickListener{action()}},LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=8})}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
}