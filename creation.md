Neues Plugin erstellen -> create-plugin.scm-manager.org

Wir starten mit dem Backend und erzeugen unsere Entität "CustomLink". Tipp `./gradlew licenseFormat`
Diese Entität repräsentiert einen Custom Link, den wir im Footer anzeigen wollen.

Damit unsere Links persistiert werden, brauchen wir einen Store. Ein Store ist eine Abstraktionsschicht, die eine oder mehrere Dateien auf dem Dateisystem umfasst. 
Innerhalb des Stores werden die Informationen im XML Dateiformat gespeichert. 
Wichtig zu wissen ist, dass die Stores beim Server Start initial geladen und im Speicher gehalten werden. 
Wenn also Updates auf den Store über SCM-Manager stattfinden, bekommen wir das mit. 
Wenn die Dateien zur Server-Laufzeit jedoch manuell geändert werden, bekommt der Server die Änderungen erst nach einem Neustart mit.
Da wir mehrere Links haben können und die Links unabhängig voneinander sind, entscheiden wir uns für einen `Configuration Entry Store`.
Wir bauen erstmal eine leere Hülle und werden dann nach TDD die einzelnen Methoden implementieren. 
