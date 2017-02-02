---=== Smartick Android ===---

    Versión mínima soportada: 4.1 (API 16)
    Versión actual de la app: 1.0.1b41 (1/01/17)

--- Gradle ---

   Las librerías externas al proyecto se añaden a través del fichero app/build.gradle.
   En la parte inferior del archivo gradle están listadas todas las depencencias externas de la aplicación.
   Por norma general no es necesario actualizar nada EXCEPTO las librerias de gms de google referentes a las notificaciones push,
   y la de facebook, ya que van eliminando acceso a determinadas versiones de la API.

   NOTA: No bajar la versión de eMMa de la 2.5.5 ya que hay un bug en las versiones anteriores que no permite recibir
   eventos necesarios para el tracking de conversión a registro.

--- XWalkView ---

    XWalkView es el Webview custom que utilizamos en la app. Es un fork del proyecto de Chromium de Google, y suelen ir siempre un par
    de versiones por detrás, ya que necesitan algo de tiempo en adaptar cada nueva versión a su framework.

    La página del proyecto en Android es: https://crosswalk-project.org/documentation/android.html
    La guía de integración en Android es: https://crosswalk-project.org/documentation/android/embedding_crosswalk.html
    Y el repositorio para nuevas versiones es: https://download.01.org/crosswalk/releases/crosswalk/android/maven2
    (cuando hay una nueva versión estable integrarla en el build.gradle y probar que no todo sigue funcionando, ya que a veces rompen cosas)

    Se ha decidido utilizar XWalkView en lugar del webview nativo de Android por varias razones:

        - Garantiza que el entorno del webview es exactamente igual para todas las instalaciones y versiones de Android donde la app se instale.
        - El rendimiento y huella en memoria es considerablemente inferior.
        - En tabletas con versiones viejas de Android el webview nativo no está basado en Chromium, sino en la implementación que haya hecho el fabricante, dando resultados
        de rendimiento muy dispares y pobres.
        -  Abre una API extendida a la del webview normal, de forma que es posible controlar aspectos de la operación del webview con mucho más detalle (carga de páginas, control de urls, control de errores, interfaz javascript-java, etc).

    Dicho esto, si en algún momento se decide elimninar el soporte para versiones antiguas de Android (el minSdk actual es 16, que es Android 4.1) sería posible
         considerar la sustitución de XWalkView por el webview normal de Android, ya basado en Chromium.

--- Generar un apk para la Play Store ---

    Para poder generar un apk en modo release hace falta firmar la app utilizando el archivo android.jks incluido en la carpeta AndroidJeyStore del proyecto.

    Para generarlo basta con hacer Build -> Generate Signed APK y completar los siguientes datos:

    Key Store Path: #PATH_ANDROID_JKS
    Key Store Password: t0p3r1t4
    Key Alias: SmartickAndroidKey
    Key password: t0p3r1t4

    Darle a next, seleccionar la carpeta de destino para el apk y poner "Build type: release". Pulsar en generar.

    Cuando el proceso haya terminado se indicará por la ventana de la consola de gradle.

--- Google Play Developer Console ---

    El perfil de management de la app de Android es el siguiente:

    user: smartickAndroid@gmail.com
    pass: Sm1rt3cK

    Y el portal de la Developer Console es: https://play.google.com/apps/publish/

    Aqui se pueden controlar todas las apps publicadas y asociadas a nuestra cuenta de Smartick.

    Para subir un apk a producción no hay más que ir al perfil de la app seleccionada, ir a la sección APK y
    subir el nuevo apk pulsando en "Upload new APK to Production".

    Los perfiles de la app tienen versión en Español y en Inglés, por lo que hay que acordarse de poner un mensaje en ambos idiomas.

    Igualmente, las capturas de pantalla y los textos tienen perfiles separados y se han de mantener por igual.


--- Google API Manager ---

    La app utiliza el servicio de notificaciones push (Google Cloud Messaging o GCM) para enviar notificaciones desde nuestro
    servidor a los dispositivos autorizados.

    Además, se utiliza el servicio de login con cuenta de Google, para lo cual es necesario generar una clave para cada app que utilice el servicio.

    Para controlar las credenciales que autorizan el uso de estos servicios basta con entrar en:

    https://console.developers.google.com/apis/credentials?project=smartick-6538c

    Las credenciales son las mismas que para la Google Play Developer Console.
