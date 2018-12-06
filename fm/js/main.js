
function init() {

    var map = L.map('map', {minZoom: 11, maxZoom:16});
    var layer = Tangram.leafletLayer({scene:'/fm/scene.yaml?t='+new Date().getTime(), attribution: 'Map data copyright OpenStreetMap contributors, ODBL; contours copyright Ordnance Survey, OS OpenData License'});
    layer.addTo(map);
    map.setView([lat, lon], zoom);

    
    var searchDlg = new Dialog('main',
        { 'OK': ()=> {searchDlg.hide(); }},
        { top: '100px', left: '100px', width: '200px',
            position: 'absolute', fontSize: '80%',
            backgroundColor: 'white',
            height: '400px',
            overflow: 'auto',
            padding: '5px',
            borderRadius: '5px',
            border: '1px solid black' });
    document.getElementById('searchBtn').addEventListener('click', e=> {
            var q = document.getElementById('q').value;
            fetch(`/fm/ws/search.php?q=${q}&format=json&outProj=4326&poi=${document.getElementById('type').value}`).then(response=>response.json()).then(json=> {
                if(json.features.length==0) {
                    alert(`No results for '${q}'`);
                } else {
                    searchDlg.show();
                    var div = document.createElement("div");
                    var h2=document.createElement("h2");
                    h2.appendChild(document.createTextNode("Search results"));
                    div.appendChild(h2);
                    json.features.forEach(f=> {
                            var p = document.createElement("p");
                            var a = document.createElement("a");
                            a.href='#';
                            a.innerHTML = f.properties.name + (f.properties.is_in ? `, ${f.properties.is_in} `:' ') +  (f.properties.featuretype || '');
                            a.addEventListener("click", e=> {
                                map.setView([f.geometry.coordinates[1], f.geometry.coordinates[0]]);
                                searchDlg.hide();
                            });
                            p.appendChild(a);
                            div.appendChild(p);
                        } );
                    searchDlg.setDOMContent(div);
                }
            } );    
        } );
    
}
