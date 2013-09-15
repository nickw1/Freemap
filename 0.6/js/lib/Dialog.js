
function Dialog(parentId,callbacks,style)
{
    this.callbacks = callbacks;
    this.style = style;
    this.parent=parentId ? (document.getElementById(parentId) || document.body):
        document.body;
    Dialog.prototype.count = (Dialog.prototype.count) ?
        Dialog.prototype.count+1 : 1;
    this.id = '_dlg' + Dialog.prototype.count;
    this.div = document.createElement("div");
    this.div.id = '_dlg' + Dialog.prototype.count;
    this.div.style.zIndex = 999;
    this.div.setAttribute("class","fmap_dlg");
    this.contentsContainer = document.createElement("div");
    this.div.appendChild(this.contentsContainer);
    var actionsContainer = document.createElement("div");
    actionsContainer.style.textAlign = 'center';
    if(this.callbacks && this.callbacks.ok)
    {
        var ok = document.createElement("input");
        ok.value="OK";
        ok.type="button";
        ok.id = this.div.id + "_ok";
        ok.onclick = this.callbacks.ok;
        actionsContainer.appendChild(ok);
    }
    if(this.callbacks && this.callbacks.cancel)
    {
        var cancel = document.createElement("input");
        cancel.value="Cancel";
        cancel.type="button";
        cancel.id = this.div.id + "_cancel";
        cancel.onclick = this.callbacks.cancel;
        actionsContainer.appendChild(cancel);
    }
    this.div.appendChild(actionsContainer);
    if(style)
        for(var s in style)
            this.div.style[s] = style[s];
}

Dialog.prototype.setContent = function(content)
{
    this.contentsContainer.innerHTML = content;
}

Dialog.prototype.setDOMContent = function(domElement)
{
    while(this.contentsContainer.childNodes.length > 0)
        this.contentsContainer.removeChild(this.contentsContainer.firstChild);
    this.contentsContainer.appendChild(domElement);
}

Dialog.prototype.show = function()
{
    this.parent.appendChild(this.div);
    this.div.style.visibility = 'visible';
}

Dialog.prototype.hide = function()
{
    this.div.style.visibility = 'hidden';
    this.parent.removeChild(this.div);
}

Dialog.prototype.isVisible = function()
{
    return this.div.style.visibility=='visible';
}

Dialog.prototype.setPosition = function(x,y)
{
    this.div.style.position='absolute';
    this.div.style.left=x+'px';
    this.div.style.top=y+'px';
}
