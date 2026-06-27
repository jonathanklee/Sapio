const LANGS = ['en', 'fr', 'de', 'it', 'es'];
const STORAGE_KEY = 'sapio_lang';

const TRANSLATIONS = {
    // ─── Header / nav ─────────────────────────────────────────────────────────
    nav_about:        { en: 'About', fr: 'À propos', de: 'Über', it: 'Informazioni', es: 'Acerca de' },

    // ─── Hero ─────────────────────────────────────────────────────────────────
    hero_eyebrow:     { en: 'Android apps that respect you!', fr: 'Des applications Android qui vous respectent !', de: 'Android-Apps, die dich respektieren!', it: 'App Android che ti rispettano!', es: '¡Apps de Android que te respetan!' },
    hero_title:       { en: 'Will your apps work without Google Play Services?', fr: 'Vos applications fonctionnent-elles sans Google Play Services ?', de: 'Funktionieren deine Apps ohne Google Play Services?', it: 'Le tue app funzionano senza Google Play Services?', es: '¿Funcionan tus apps sin Google Play Services?' },
    hero_subtitle:    { en: 'Community compatibility for deGoogled Android — microG and bare AOSP.', fr: 'Compatibilité communautaire pour Android dégooglisé — microG et bare AOSP.', de: 'Community-Kompatibilität für entgoogeltes Android — microG und bare AOSP.', it: 'Compatibilità della community per Android deGooglizzato — microG e bare AOSP.', es: 'Compatibilidad comunitaria para Android desgooglizado — microG y bare AOSP.' },
    search_placeholder: { en: 'Search an app, e.g. Netflix or com.netflix.mediaclient', fr: 'Rechercher une application, ex. Netflix ou com.netflix.mediaclient', de: 'App suchen, z. B. Netflix oder com.netflix.mediaclient', it: 'Cerca un\'app, es. Netflix o com.netflix.mediaclient', es: 'Busca una app, p. ej. Netflix o com.netflix.mediaclient' },
    examples_try:     { en: 'Try', fr: 'Essayez', de: 'Probiere', it: 'Prova', es: 'Prueba' },
    stat_apps:        { en: 'apps', fr: 'applications', de: 'Apps', it: 'app', es: 'apps' },
    stat_evaluations: { en: 'evaluations', fr: 'évaluations', de: 'Bewertungen', it: 'valutazioni', es: 'evaluaciones' },
    stat_community:   { en: 'community-driven', fr: 'par la communauté', de: 'von der Community', it: 'dalla community', es: 'impulsado por la comunidad' },
    trust_no_account: { en: 'No account', fr: 'Sans compte', de: 'Kein Konto', it: 'Nessun account', es: 'Sin cuenta' },
    trust_no_tracking:{ en: 'No tracking', fr: 'Sans pistage', de: 'Kein Tracking', it: 'Nessun tracciamento', es: 'Sin rastreo' },
    trust_open_source:{ en: 'Open source', fr: 'Open source', de: 'Open Source', it: 'Open source', es: 'Código abierto' },
    trust_public_api: { en: 'Public API', fr: 'API publique', de: 'Öffentliche API', it: 'API pubblica', es: 'API pública' },

    // ─── Results ──────────────────────────────────────────────────────────────
    results_latest:   { en: 'Latest evaluations', fr: 'Dernières évaluations', de: 'Neueste Bewertungen', it: 'Ultime valutazioni', es: 'Últimas evaluaciones' },
    show_unsafe:      { en: 'Show unsafe environments', fr: 'Montrer les environnements non sécurisés', de: 'Unsichere Umgebungen anzeigen', it: 'Mostra ambienti non sicuri', es: 'Mostrar entornos no seguros' },
    legend_works:     { en: 'Perfect', fr: 'Parfait', de: 'Perfekt', it: 'Perfetto', es: 'Perfecto' },
    legend_partial:   { en: 'Partial', fr: 'Partiel', de: 'Teilweise', it: 'Parziale', es: 'Parcial' },
    legend_broken:    { en: 'Unusable', fr: 'Inutilisable', de: 'Unbrauchbar', it: 'Inutilizzabile', es: 'Inutilizable' },
    empty_title:      { en: 'No results', fr: 'Aucun résultat', de: 'Keine Ergebnisse', it: 'Nessun risultato', es: 'Sin resultados' },
    empty_hint:       { en: 'Try searching by app name or package name (e.g. com.netflix.mediaclient).', fr: 'Essayez par nom d\'application ou nom de paquet (ex. com.netflix.mediaclient).', de: 'Suche nach App- oder Paketname (z. B. com.netflix.mediaclient).', it: 'Prova con il nome dell\'app o del pacchetto (es. com.netflix.mediaclient).', es: 'Prueba por nombre de la app o del paquete (p. ej. com.netflix.mediaclient).' },
    error_title:      { en: 'Could not reach the server', fr: 'Impossible de joindre le serveur', de: 'Server nicht erreichbar', it: 'Impossibile raggiungere il server', es: 'No se pudo conectar al servidor' },
    error_hint:       { en: 'Please try again later.', fr: 'Veuillez réessayer plus tard.', de: 'Bitte später erneut versuchen.', it: 'Riprova più tardi.', es: 'Inténtalo de nuevo más tarde.' },

    // ─── Evaluation labels (rendered) ─────────────────────────────────────────
    rating_1:         { en: 'Perfect', fr: 'Parfait', de: 'Perfekt', it: 'Perfetto', es: 'Perfecto' },
    rating_2:         { en: 'Partial', fr: 'Partiel', de: 'Teilweise', it: 'Parziale', es: 'Parcial' },
    rating_3:         { en: 'Unusable', fr: 'Inutilisable', de: 'Unbrauchbar', it: 'Inutilizzabile', es: 'Inutilizable' },
    env_secure:       { en: 'secure', fr: 'sécurisé', de: 'sicher', it: 'sicuro', es: 'seguro' },
    env_unsafe:       { en: 'unsafe', fr: 'non sécurisé', de: 'unsicher', it: 'non sicuro', es: 'no seguro' },
    doesnt_work:      { en: "Doesn't work", fr: 'Ne fonctionne pas', de: 'Funktioniert nicht', it: 'Non funziona', es: 'No funciona' },
    feat_notifications:   { en: 'Notifications', fr: 'Notifications', de: 'Benachrichtigungen', it: 'Notifiche', es: 'Notificaciones' },
    feat_in_app_purchase: { en: 'In-app purchases', fr: 'Achats intégrés', de: 'In-App-Käufe', it: 'Acquisti in-app', es: 'Compras integradas' },
    feat_login:           { en: 'Login', fr: 'Connexion', de: 'Anmeldung', it: 'Accesso', es: 'Inicio de sesión' },
    feat_maps:            { en: 'Maps', fr: 'Cartes', de: 'Karten', it: 'Mappe', es: 'Mapas' },
    feat_location:        { en: 'Location', fr: 'Localisation', de: 'Standort', it: 'Posizione', es: 'Ubicación' },
    feat_payments:        { en: 'Physical payments', fr: 'Paiements physiques', de: 'Physische Zahlungen', it: 'Pagamenti fisici', es: 'Pagos físicos' },
    feat_cast:            { en: 'Screen casting', fr: 'Diffusion d\'écran', de: 'Bildschirmübertragung', it: 'Trasmissione schermo', es: 'Proyección de pantalla' },
    feat_augmented_reality:{ en: 'Augmented reality', fr: 'Réalité augmentée', de: 'Augmented Reality', it: 'Realtà aumentata', es: 'Realidad aumentada' },

    // ─── Contribute ───────────────────────────────────────────────────────────
    contribute_title: { en: 'Want to contribute?', fr: 'Envie de contribuer ?', de: 'Möchtest du beitragen?', it: 'Vuoi contribuire?', es: '¿Quieres contribuir?' },
    contribute_text:  { en: 'Evaluations come from the community, through the app. Install Sapio on your deGoogled device to rate apps and share their compatibility with everyone.', fr: 'Les évaluations viennent de la communauté, via l\'application. Installez Sapio sur votre appareil dégooglisé pour évaluer les applications et partager leur compatibilité avec tous.', de: 'Die Bewertungen stammen von der Community, über die App. Installiere Sapio auf deinem entgoogelten Gerät, um Apps zu bewerten und ihre Kompatibilität mit allen zu teilen.', it: 'Le valutazioni provengono dalla community, tramite l\'app. Installa Sapio sul tuo dispositivo deGooglizzato per valutare le app e condividerne la compatibilità con tutti.', es: 'Las evaluaciones provienen de la comunidad, a través de la app. Instala Sapio en tu dispositivo desgooglizado para evaluar apps y compartir su compatibilidad con todos.' },
    contribute_fdroid:{ en: 'Get it on F-Droid', fr: 'Télécharger sur F-Droid', de: 'Bei F-Droid laden', it: 'Scarica su F-Droid', es: 'Consíguelo en F-Droid' },
    contribute_github:{ en: 'GitHub releases', fr: 'Versions GitHub', de: 'GitHub-Releases', it: 'Release GitHub', es: 'Versiones de GitHub' },

    // ─── About ────────────────────────────────────────────────────────────────
    about_title:      { en: 'What is Sapio?', fr: 'Qu\'est-ce que Sapio ?', de: 'Was ist Sapio?', it: 'Cos\'è Sapio?', es: '¿Qué es Sapio?' },
    about_p1:         { en: 'Sapio is the anagram of Open Source API. It provides the compatibility of an Android application running on a device without Google Play Services — i.e. deGoogled bare AOSP devices, coupled or not with microG.', fr: 'Sapio est l\'anagramme d\'Open Source API. Il fournit la compatibilité d\'une application Android sur un appareil sans Google Play Services — c\'est-à-dire des appareils AOSP dégooglisés, associés ou non à microG.', de: 'Sapio ist das Anagramm von Open Source API. Es liefert die Kompatibilität einer Android-App auf einem Gerät ohne Google Play Services — also entgoogelte bare-AOSP-Geräte, mit oder ohne microG.', it: 'Sapio è l\'anagramma di Open Source API. Fornisce la compatibilità di un\'applicazione Android su un dispositivo senza Google Play Services — cioè dispositivi AOSP deGooglizzati, con o senza microG.', es: 'Sapio es el anagrama de Open Source API. Indica la compatibilidad de una aplicación Android en un dispositivo sin Google Play Services — es decir, dispositivos AOSP desgooglizados, con o sin microG.' },
    about_p2:         { en: 'Evaluations in Sapio are given to the community by the community. Sapio can serve as a lobbying tool by sharing compatibility on social media to raise awareness among app developers about respecting users\' personal data.', fr: 'Les évaluations de Sapio sont fournies à la communauté par la communauté. Sapio peut servir d\'outil de pression en partageant la compatibilité sur les réseaux sociaux pour sensibiliser les développeurs au respect des données personnelles.', de: 'Die Bewertungen in Sapio stammen von der Community für die Community. Sapio kann als Lobbying-Werkzeug dienen, indem die Kompatibilität in sozialen Medien geteilt wird, um App-Entwickler für den Schutz persönlicher Daten zu sensibilisieren.', it: 'Le valutazioni di Sapio sono fornite alla community dalla community. Sapio può fungere da strumento di sensibilizzazione condividendo la compatibilità sui social per sensibilizzare gli sviluppatori sul rispetto dei dati personali.', es: 'Las evaluaciones de Sapio las aporta la comunidad para la comunidad. Sapio puede servir como herramienta de presión compartiendo la compatibilidad en redes sociales para concienciar a los desarrolladores sobre el respeto a los datos personales.' },
    about_star:       { en: '⭐ Star on GitHub', fr: '⭐ Mettre une étoile sur GitHub', de: '⭐ Auf GitHub mit Stern markieren', it: '⭐ Metti una stella su GitHub', es: '⭐ Destacar en GitHub' },

    // ─── Footer ───────────────────────────────────────────────────────────────
    disclaimer_label: { en: 'Disclaimer', fr: 'Avertissement', de: 'Haftungsausschluss', it: 'Avvertenza', es: 'Aviso' },
    disclaimer_text:  { en: 'Evaluations are community-contributed and may be inaccurate, incomplete, or device-specific. Sapio and its maintainers are not responsible for any issues arising from relying on these evaluations.', fr: 'Les évaluations sont fournies par la communauté et peuvent être inexactes, incomplètes ou spécifiques à un appareil. Sapio et ses mainteneurs ne sont pas responsables des problèmes liés à leur utilisation.', de: 'Die Bewertungen stammen von der Community und können ungenau, unvollständig oder gerätespezifisch sein. Sapio und seine Betreuer haften nicht für Probleme, die aus der Nutzung dieser Bewertungen entstehen.', it: 'Le valutazioni sono fornite dalla community e possono essere inesatte, incomplete o specifiche per un dispositivo. Sapio e i suoi manutentori non sono responsabili di eventuali problemi derivanti dal loro utilizzo.', es: 'Las evaluaciones las aporta la comunidad y pueden ser inexactas, incompletas o específicas de un dispositivo. Sapio y sus mantenedores no se hacen responsables de los problemas derivados de su uso.' },
    footer_credits_by:{ en: 'Brain icons by', fr: 'Icônes de cerveau par', de: 'Gehirn-Icons von', it: 'Icone del cervello di', es: 'Iconos de cerebro por' },

    // ─── App detail page ──────────────────────────────────────────────────────
    back_all:         { en: '← All evaluations', fr: '← Toutes les évaluations', de: '← Alle Bewertungen', it: '← Tutte le valutazioni', es: '← Todas las evaluaciones' },
    app_not_found_title: { en: 'App not found', fr: 'Application introuvable', de: 'App nicht gefunden', it: 'App non trovata', es: 'App no encontrada' },
    app_not_found_hint:  { en: 'We couldn\'t find an evaluation for this app.', fr: 'Aucune évaluation trouvée pour cette application.', de: 'Für diese App wurde keine Bewertung gefunden.', it: 'Nessuna valutazione trovata per questa app.', es: 'No encontramos ninguna evaluación para esta app.' },
    browse_all:       { en: 'Browse all evaluations', fr: 'Parcourir toutes les évaluations', de: 'Alle Bewertungen ansehen', it: 'Sfoglia tutte le valutazioni', es: 'Ver todas las evaluaciones' },
    unsafe_only_hint: { en: 'Only unsafe-environment evaluations exist for this app. Enable "Show unsafe environments" to see them.', fr: 'Seules des évaluations en environnement non sécurisé existent pour cette application. Activez « Montrer les environnements non sécurisés » pour les voir.', de: 'Für diese App gibt es nur Bewertungen in unsicheren Umgebungen. Aktiviere „Unsichere Umgebungen anzeigen", um sie zu sehen.', it: 'Per questa app esistono solo valutazioni in ambienti non sicuri. Attiva "Mostra ambienti non sicuri" per vederle.', es: 'Para esta app solo existen evaluaciones en entornos no seguros. Activa «Mostrar entornos no seguros» para verlas.' },

    // ─── Dynamic phrases ──────────────────────────────────────────────────────
    results_for:      { en: 'Results for "%s"', fr: 'Résultats pour « %s »', de: 'Ergebnisse für „%s"', it: 'Risultati per "%s"', es: 'Resultados para «%s»' },
    count_apps_one:   { en: '%n app', fr: '%n application', de: '%n App', it: '%n app', es: '%n app' },
    count_apps_other: { en: '%n apps', fr: '%n applications', de: '%n Apps', it: '%n app', es: '%n apps' },
    language_label:   { en: 'Language', fr: 'Langue', de: 'Sprache', it: 'Lingua', es: 'Idioma' },
    summary_frame:    { en: '%name without Google Play Services — %parts.', fr: '%name sans Google Play Services — %parts.', de: '%name ohne Google Play Services — %parts.', it: '%name senza Google Play Services — %parts.', es: '%name sin Google Play Services — %parts.' },
    summary_none:     { en: 'No evaluation yet for %name without Google Play Services.', fr: 'Pas encore d\'évaluation pour %name sans Google Play Services.', de: 'Noch keine Bewertung für %name ohne Google Play Services.', it: 'Ancora nessuna valutazione per %name senza Google Play Services.', es: 'Aún no hay evaluación para %name sin Google Play Services.' },
    summary_no_prefix:{ en: 'no', fr: 'sans', de: 'ohne', it: 'senza', es: 'sin' },
};

const LANG_NAMES = { en: 'English', fr: 'Français', de: 'Deutsch', it: 'Italiano', es: 'Español' };

let currentLang = detectLanguage();

function detectLanguage() {
    if (typeof localStorage !== 'undefined') {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored && LANGS.includes(stored)) {
            return stored;
        }
    }

    if (typeof navigator !== 'undefined') {
        const preferred = navigator.languages?.length ? navigator.languages : [navigator.language];
        for (const tag of preferred) {
            const code = tag?.slice(0, 2).toLowerCase();
            if (code && LANGS.includes(code)) {
                return code;
            }
        }
    }

    return 'en';
}

function getLang() {
    return currentLang;
}

function setLang(lang) {
    if (!LANGS.includes(lang)) { return; }

    localStorage.setItem(STORAGE_KEY, lang);
    currentLang = lang;
    location.reload();
}

function t(key) {
    const entry = TRANSLATIONS[key];
    if (!entry) { return key; }

    return entry[currentLang] ?? entry.en;
}

function format(key, replacements) {
    let text = t(key);
    for (const [token, value] of Object.entries(replacements)) {
        text = text.replace(token, value);
    }

    return text;
}

function relativeDate(isoString) {
    if (!isoString) { return null; }

    const diffMs = new Date(isoString).getTime() - Date.now();
    const rtf = new Intl.RelativeTimeFormat(currentLang, { numeric: 'auto', style: 'short' });
    const units = [
        ['year', 31536000000],
        ['month', 2592000000],
        ['day', 86400000],
        ['hour', 3600000],
        ['minute', 60000],
    ];

    for (const [unit, ms] of units) {
        const value = Math.round(diffMs / ms);
        if (Math.abs(value) >= 1 || unit === 'minute') {
            return rtf.format(value, unit);
        }
    }

    return rtf.format(0, 'minute');
}

function applyStaticTranslations(root = document) {
    document.documentElement.lang = currentLang;

    for (const el of root.querySelectorAll('[data-i18n]')) {
        el.textContent = t(el.dataset.i18n);
    }

    for (const el of root.querySelectorAll('[data-i18n-placeholder]')) {
        el.setAttribute('placeholder', t(el.dataset.i18nPlaceholder));
    }
}

function initLanguageSwitcher() {
    const select = document.getElementById('lang-select');
    if (!select) { return; }

    select.innerHTML = '';
    for (const lang of LANGS) {
        const option = document.createElement('option');
        option.value = lang;
        option.textContent = LANG_NAMES[lang];
        option.selected = lang === currentLang;
        select.appendChild(option);
    }

    select.addEventListener('change', () => setLang(select.value));
}

function setupI18n() {
    applyStaticTranslations();
    initLanguageSwitcher();
}

export {
    LANGS,
    LANG_NAMES,
    getLang,
    setLang,
    t,
    format,
    relativeDate,
    applyStaticTranslations,
    setupI18n,
};
