
function Admin(resultsDivId,countyCbxId,districtCbxId,parishCbxId)
{
    this.div = document.getElementById(resultsDivId);
    this.ajax = new Ajax();
    this.cbx =  {} ;
    this.cbx.county = document.getElementById(countyCbxId);
    this.cbx.district = document.getElementById(districtCbxId);
    this.cbx.parish = document.getElementById(parishCbxId);
    this.populateWithProblems();
    this.populateCbx ({ cat: 'county'});
}

Admin.prototype.populateWithProblems = function(options)
{
    var qs="action=getProblemsByAdmUnit", first=true;
    if(options)
    {
        for(k in options)
        {
            if(options[k]!="none")
                qs+='&'+k+'='+options[k];
        }
    }
    new Ajax().sendRequest ( 'problem.php',
                            { method: 'GET',
                              parameters: qs, 
                              callback: this.pcallback.bind(this) }
                              );
}

Admin.prototype.pcallback = function(xmlHTTP)
{
    while(this.div.childNodes.length > 0)
        this.div.removeChild(this.div.firstChild);

    var table = document.createElement("table");
    var tr = document.createElement("tr");
    var headings = [ "County", "District", "Parish", "Route num", "Problem",
                        "Status", "Action" ];


    for(var i=0; i<headings.length; i++)
    {
        var th=document.createElement("th");
        th.appendChild(document.createTextNode(headings[i]));
        tr.appendChild(th);
    }
    table.appendChild(tr);
    var json = JSON.parse(xmlHTTP.responseText);
    for(var i=0; i<json.features.length; i++)
    {
        var tr = document.createElement("tr");
        tr.id = 'tr' + json.features[i].properties.id;
        var data = [ json.features[i].properties.county,
                    json.features[i].properties.district,
                    json.features[i].properties.parish,
                    json.features[i].properties.routeno,
                    json.features[i].properties.problem ,
                    json.features[i].properties.status ]; 
        for(var j=0; j<headings.length; j++)
        {
            var td=document.createElement("td");
            if(j<data.length)
                td.appendChild(document.createTextNode(data[j]));
            if(headings[j]=="Status")
                td.id='fix'+json.features[i].properties.id;
            else if (headings[j]=="Action")
            {
                var a = document.createElement('a');
                a.href='index.php?probid='+json.features[i].properties.id;
                a.appendChild(document.createTextNode("Map"));
                td.appendChild(a);
                td.appendChild(document.createTextNode(" | "));
                 var a2 = document.createElement('a');
                a2.href='#';
                a2.appendChild(document.createTextNode("Log"));
                a2.onclick = (function(id) 
                                {
                                    return this.getLog.bind(this,id);
                                }).call(this,json.features[i].properties.id);
                td.appendChild(a2);
                td.appendChild(document.createTextNode(" | "));
                if(json.features[i].properties.status== "Reported")
                {
                    var a3 = document.createElement("a");
                    a3.href='#';
                    a3.id = 'fixlink' + json.features[i].properties.id; 
                    a3.onclick = (function(id)
                    {
                        new Ajax().sendRequest ('problem.php',
                            { method: 'POST',
                                parameters: 'id=' + id + '&action=fix',
                                callback : function(xmlHTTP) 
                                { 
                                    document.getElementById('fix'+id).
                                        innerHTML = 'Fixed';
                                    td.removeChild
                                        (document.getElementById('fixlink'+id));
                                }
                            } );
                    } ).bind(this,json.features[i].properties.id);
                    a3.appendChild(document.createTextNode("FIX!"));
                    td.appendChild(a3);
                    td.appendChild(document.createTextNode(" | "));
                }
                var a4 = document.createElement("a");
                a4.href='#';
                a4.onclick = (function(id)
                {
                    new Ajax().sendRequest ('problem.php',
                            { method: 'POST',
                                parameters: 'id=' + id + '&action=remove',
                                callback : function(xmlHTTP) 
                                { 
                                    table.removeChild
                                        (document.getElementById('tr'+id));
                                }
                            } );
                } ).bind(this,json.features[i].properties.id);
                a4.appendChild(document.createTextNode("Remove"));
                td.appendChild(a4);
            }//if status
            tr.appendChild(td);
        }//j
        table.appendChild(tr);
    }//i
    this.div.appendChild(table);
}

Admin.prototype.populateCbx = function(options)
{
    if(true)
    {
        var qs="", first=true;
        for(k in options)
        {
            if(first==false)
                qs+="&";
            else
                first=false;
            qs += k + "=" + options[k];
        }
        

        new Ajax().sendRequest ('admunits.php',
                        { method: 'GET',
                        parameters: qs, 
                        callback: this.populateCbxCallback.bind(this) }
                        , options.cat
                        );
    }
}

Admin.prototype.populateCbxCallback = function(xmlHTTP, cat)
{
    var cbx = this.cbx[cat];
    while(cbx.childNodes.length > 0)
        cbx.removeChild(cbx.firstChild);
    var results = JSON.parse(xmlHTTP.responseText);
    cbx.options[0] = new Option("Please select","none");
    cbx.options[1] = new Option("all","all");
    for(var i=0; i<results.length; i++)
        cbx.options[i+2] = new Option(results[i]);
    cbx.onchange = this.populateWithSelectedProblems.bind(this,cat);
}

Admin.prototype.populateWithSelectedProblems  = function(cat)
{
    var subcats = { 'county' : 'district',
                    'district': 'parish' };

    if(true)
    {
        this.populateWithProblems
            ( { county: this.cbx.county.value,
                district: this.cbx.district.value,
                parish: this.cbx.parish.value } );
    }

    if(subcats[cat])
    {
        var obj = {};
        obj["cat"] = subcats[cat];
        obj[cat] = this.cbx[cat].value; 
        if(this.cbx[cat].selectedIndex>0)
            this.populateCbx (obj); 
    }
}

Admin.prototype.getLog = function(id)
{
    new Ajax().sendRequest
        ("problem.php",
            { method: "GET",
              parameters: "action=getLog&id="+id,
              callback: (function (xmlHTTP)
                              {
                                while(this.div.childNodes.length>0)
                                    this.div.removeChild(this.div.firstChild);
                                var h2 = document.createElement("h2");
                                var h2text = "Log for problem " + id;
                                h2.appendChild(document.createTextNode(h2text));
                                this.div.appendChild(h2);
                                var ul = document.createElement("ul");
                                var json = JSON.parse(xmlHTTP.responseText);
                                for(var i=0; i<json.length; i++)
                                {
                                    var li=document.createElement("li");
                                    li.appendChild
                                        (document.createTextNode(json[i].log));
                                    var em = document.createElement("em");
                                    em.appendChild
                                        (document.createTextNode
                                            (" "+ json[i].subdate));
                                    li.appendChild(em);
                                    ul.appendChild(li);
                                }
                                this.div.appendChild(ul);
                                var h3 = document.createElement("h3");
                                h3.appendChild
                                    (document.createTextNode
                                        ("Add a new log entry:"));
                                this.div.appendChild(h3);
                                var textarea = document.createElement
                                    ("textarea");
                                textarea.id="newlog";
                                this.div.appendChild(textarea);
                                var btn= document.createElement("input");
                                btn.type="button";
                                btn.value="Send!";
                                btn.onclick = function()
                                {
                                    var newlog=document.getElementById
                                        ('newlog').value;
                                    var qs = 'id=' + id + 
                                        '&msg=' + newlog + 
                                                '&action=addToLog';
                                    new Ajax().sendRequest
                                        ("problem.php",
                                            { method: "POST",
                                              parameters:  qs,
                                             callback: function(xmlHTTP)
                                                 { 
                                                    var li =
                                                        document.createElement
                                                        ("li");
                                                    li.appendChild
                                                        (document.createTextNode
                                                            (newlog));
                                                    ul.appendChild(li);
                                                }
                                            }
                                        );
                               };
                               this.div.appendChild(btn);
                            }
                          ).bind(this)
            }
        );
}

function init()
{
    new Admin("problems","county","district","parish");
}
