
// Provide (simple) bind if it doesn't exist
if (! Function.prototype.bind)
{
    Function.prototype.bind = function(target)
    {
		var args = new Array(); 
		for(var i=1; i<arguments.length; i++)
			args.push(arguments[i]);
		
        var self=this;
        return function() 
		{ 
			for(var i=0; i<arguments.length; i++)
				args.push(arguments[i]);
			self.apply(target,args); 
		} 
    }
}


if (! Object.prototype.create)
{
    Object.prototype.create = function(ptype)
    {
        function O() { }
        var o = new O();
        o.prototype = ptype; 
        return o;
    }
}
