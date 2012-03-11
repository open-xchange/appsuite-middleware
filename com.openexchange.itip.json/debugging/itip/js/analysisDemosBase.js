(function () {
    
    function Environment () {
        var that = this;
        this.deferreds = [];
        this.initialisation = new $.Deferred();
        this.doneDeferred = new $.Deferred();
        this.enqueue(this.doneDeferred);
        
        that.user = {
        
        };
        that.apps = {
            
        };
        
        var emailRetrieved = new $.Deferred();
        var foldersRetrieved = new $.Deferred();

        $.get("/ajax/user?action=get&session="+ox.session).always(function (resp) {
           resp = JSON.parse(resp.responseText);
           that.user = resp.data;
           emailRetrieved.resolve();
        });

        $.get("/ajax/config/folder?session="+ox.session).always(function (resp) {
            resp = JSON.parse(resp.responseText);
            that.folders = resp.data;
            foldersRetrieved.resolve();
        });
        
        $.when(emailRetrieved, foldersRetrieved).then(function () {
           that.initialisation.resolve(); 
        });
    
        this.getICSRenderingEnvironment = function () {
            return $.extend({
                user : this.user,
                date: function () {
                    return function (text, render) {
                        var parsed = Date.parse(text);
                        if (parsed === null) {
                            console.log("Could not parse date: ", text);
                        }
                        return parsed.toString("yyyyMMddTHHmmss")+"Z\n";
                    };
                },
                dateEval: function () {
                    return function (text, render) {
                        var date = (new Function("return "+text))();
                        return date.toString("yyyyMMddTHHmmss")+"Z\n";
                    }
                }
            }, this.apps);
        };
        
        this.participants = {
            currentUser: function () {
                return {
                    id: that.user.id,
                    type: 1
                }
            },
            external: function (email) {
                return {
                    mail: email,
                    type: 5
                };
            }
        }
    
    };
    
    Environment.prototype.start = function (state) {
        this.state = state;
    }

    Environment.prototype.loadICS = function (name) {
        var self = this;
        var d = new $.Deferred();
        var internalD = new $.Deferred();
        this.enqueue(d);
        $.get("itipSamples/"+name+"?cacheBuster="+Math.random()).always(function(resp) {
            var ical = resp;
            ical = Mustache.to_html(ical, self.getICSRenderingEnvironment());
            d.resolve(ical);
        });
        return d;
    }

    Environment.prototype.analyze = function (demoName) {
        var self = this;
        var d = new $.Deferred();
        this.enqueue(d);
        this.loadICS(demoName).done(function (icsText) {
             $.ajax("/ajax/calendar/itip?action=analyze&session="+ox.session+"&timezone=UTC", {
                   type: "PUT",
                   data: JSON.stringify({ical: icsText})
               }).always(function (resp) {
                  resp = JSON.parse(resp.responseText);
                  self.trigger("analysis", {
                      name: demoName,
                      ical: icsText,
                      resp: resp,
                      env: self
                  });
                  d.resolve({
                        name: demoName,
                        ical: icsText,
                        resp: resp
                    });
               }); 
        });
        return d;
    }

    Environment.prototype.on = function (evt, cb) {
        this.listeners = this.listeners || {};
    
        this.listeners[evt] = this.listeners[evt] || [];
    
        this.listeners[evt].push(cb);
    };

    Environment.prototype.trigger = function (evt, payload) {
        if (!this.listeners) {
            return;
        }
        var list = this.listeners[evt] || [];
    
        _(list).each(function(cb) {
            cb(payload);
        });
    }
    
    Environment.prototype.enqueue = function(deferred) {
        var self = this;
        this.deferreds.push(deferred);
        deferred.always(function () {
            var done = true;
            _(self.deferreds).each(function (d) {
                if (d.state() === "pending") {
                    done = false;
                }
            });
            if (done) {
                self.trigger(self.state);
            }
        });
    }
    
    Environment.prototype.cleanUp = function () {
        if (this.testFolder) {
            $.ajax("/ajax/folders?action=delete&session="+ox.session, {
                type: "PUT",
                data: "["+this.testFolder.id+"]"
            });
        }
    }
    
    Environment.prototype.done = function () {
        this.doneDeferred.resolve();
    }
    
    Environment.prototype.date = function (string) {
        var date = Date.parse(string);
        return this.timestamp(date);
    }
    
    Environment.prototype.timestamp = function (date) {
        return date.setTimezoneOffset(0).getTime();
    }
    
    Environment.prototype.createAppointment = function(name, app) {
        var self = this;
        var d = new $.Deferred();
        this.enqueue(d);
        this.getTestFolder().done(function (folder) {
            app.folder_id = folder.id;
            if (!app.title) {
                app.title = name;
            }
            
            $.ajax("/ajax/calendar?action=new&session="+ox.session+"&folder="+app.folder_id+"&timezone=UTC", {
                type: "PUT",
                data: JSON.stringify(app)
            }).always(function (resp) {
                resp = JSON.parse(resp.responseText);
                $.get("/ajax/calendar?action=get&session="+ox.session+"&id="+resp.data.id+"&folder="+app.folder_id+"&timezone=UTC").always(function (resp) {
                    self.apps[name] = JSON.parse(resp.responseText).data;
                    console.log("CREATED APPOINTMENT: ", self.apps[name]);
                    d.resolve();
                });
            });
        });
        return d;
    }
    
    Environment.prototype.createException = function(name, app) {
        var self = this;
        var d = new $.Deferred();
        this.enqueue(d);
        this.getTestFolder().done(function (folder) {
            app.folder_id = folder.id;
            if (!app.title) {
                app.title = name;
            }
            
            $.ajax("/ajax/calendar?action=update&session="+ox.session+"&folder="+app.folder_id+"&timezone=UTC&id="+app.id+"&timestamp=9223372036854775806", {
                type: "PUT",
                data: JSON.stringify(app)
            }).always(function (resp) {
                resp = JSON.parse(resp.responseText);
                $.get("/ajax/calendar?action=get&session="+ox.session+"&id="+resp.data.id+"&folder="+app.folder_id+"&timezone=UTC").always(function (resp) {
                    self.apps[name] = JSON.parse(resp.responseText).data;
                    console.log("CREATED EXCEPTION: ", self.apps[name]);
                    d.resolve();
                });
            });
        });
        return d;
    }
    
    
    Environment.prototype.getTestFolder = function() {
        var self = this;
        if (this.testFolderLoadDeferred) {
            return this.testFolderLoadDeferred;
        }
        this.testFolderLoadDeferred = new $.Deferred();
        var d = this.testFolderLoadDeferred;
        this.enqueue(d);

        $.ajax({
            type: "PUT",
            url: "/ajax/folders?action=new&session="+ox.session+"&folder_id="+this.folders.calendar,
            data: JSON.stringify({
                folder_id: this.folders.calendar,
                title: "itipDemos-"+(Math.floor(Math.random() * 1000)), 
                module: "calendar",
                type: "private",
                permissions: [
                    {
                        bits: 403710016,
                        entity: self.user.id,
                        group: false
                    }
                ]
            })
        }).always(function (resp) {
           resp = JSON.parse(resp.responseText);
           // Load the new folder
           $.get("/ajax/folders?action=get&session="+ox.session+"&id="+resp.data).always(function (resp) {
               self.testFolder = JSON.parse(resp.responseText).data;
               d.resolve(self.testFolder);
           });
        });
        
        return d;
        
    }
    
    Environment.prototype.perform = function (action, icsText) {
        var self = this;
        var d = new $.Deferred();
        this.enqueue(d);
        $.ajax("/ajax/calendar/itip?action="+action+"&session="+ox.session+"&timezone=UTC", {
              type: "PUT",
              data: JSON.stringify({ical: icsText})
        }).always(function (resp) {
            resp = JSON.parse(resp.responseText);
            self.trigger("performed", {
                action: action,
                ical: icsText,
                resp: resp,
                env: self
            });
            d.resolve({
                action: action,
                ical: icsText,
                resp: resp,
                env: self
            });
        });
    }
    
    // Class Demo

    function Demo (name) {
        this.name = name;
    }
    
    Demo.prototype.setup = function (setupBlock) {
        this.setupBlock = setupBlock;
        return this;
    }

    Demo.prototype.execution = function (execBlock) {
        this.execBlock = execBlock;
        return this;
    }

    Demo.prototype.run = function (env) {
        var self = this;
        if (this.setupBlock) {
            env.start("setup");
            env.on("setup", function () {
                if (self.execBlock) {
                    env.start("execution");
                    self.execBlock(env);
                } 
            });
            this.setupBlock(env);
            return;
        } else if (this.execBlock) {
            env.start("execution");
            this.execBlock(env);
        }
    }
    
    

    // Class DemoCollection

    function DemoCollection () {
        this.demos = {};
    }

    DemoCollection.prototype.run = function (name) {
        var self = this;
        var env = new Environment()
        env.initialisation.done(function () {
            self.trigger("run", {name: name});
            env.on("analysis", function (payload) {
                self.trigger("analysis", payload);
            })
            self.demos[name].run(env);
            
            env.on("execution", function () {
                self.trigger("done", {name: name});
                env.cleanUp();
            })
        });
    
    }
    
    DemoCollection.prototype.offerActions = function (name) {
        var self = this;
        var env = new Environment()
        env.initialisation.done(function () {
            self.trigger("run", {name: name});
            env.on("analysis", function (payload) {
                self.trigger("analysis", payload);
            })
            self.demos[name].run(env);
            
            env.on("performed", function (evt) {
                self.trigger("performed", evt);
            })
        });
    
    }

    DemoCollection.prototype.define = function (name) {
        this.demos[name] = new Demo(name);
        return this.demos[name];
    }

    DemoCollection.prototype.on = function (evt, cb) {
        this.listeners = this.listeners || {};
    
        this.listeners[evt] = this.listeners[evt] || [];
    
        this.listeners[evt].push(cb);
    };

    DemoCollection.prototype.trigger = function (evt, payload) {
        if (!this.listeners) {
            return;
        }
        var list = this.listeners[evt] || [];
    
        _(list).each(function(cb) {
            cb(payload);
        });
    }

    ox.demos = new DemoCollection();
})();
