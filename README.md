# BE-QoS

Ce depot contient tous les documents modifiés ou créés dans le cadre du BE-QoS 2023. Le mode de fonctionnement des appels est explicité via le diagramme de sequence (diagramme seq à OUVRIR sur draw.io) avec notamment les informations échangées lors d'un INVITE comme les numéros de ports et @IP correctes. Slides de la présentation accessibles via le document BE QoS Propositon.pptx.

# Les fonctions modifiées/crées pour le proxy sont : processIncomingResponse (ligne 2327) et forwardToBandwidthBroker (ligne 2877). 
- Envoi du proxy au bandwidth broker la requete ou fin d'appel, qui vérifie la disponibilité du réseau (variable représentant la bandwidth totale disponible, mise à jour à chaque nouvel appel), et tient un compte des appels en cours via une ArrayList, afin de n'envoyer qu'une seule requete/fin de ressources au TraficController (le proxy forward toutes les INVITEs, hors il y en a plusieurs par appel). 
- Le proxy est à lancer sur la bonne machine (celle-ci utilisant les ports 5060 pour ses service de proxy ne peut plus être client). On peut lancer ces services depuis le binaire exécutable ( dans le répertoire workspace/openjsip/bin) : ./openjsip start all


# Fonction du BandwidthBroker qui verifie le type de requête et l'envoie au TC: processRequest
- Reception de la requete de liberation ou reservation par le TC, qui met en place un shaping des paquets sortants a chaque routeur linux d'entrée/sortie du réseau client (champs TOS = 46). Ce shaping est réalisé par l'execution d'un fichier python qui reçoit les requêtes du BB et écrits des commandes sous linux. 
- Le Bandwidth Broker est lancé depuis un des routeurs Linux. Afin de l'exécuter, il suffit de taper : java BandwidthBroker   (dans le répertoire où doit se trouver BandwidthBroker.java, ClientsConnection.java, Reseau.java)

# Fichier tcp_qos_server.py et PEP.sh
- Les paquets notés prioritaires par les routeurs du réseau client sont reconnus par le réseau de coeur qui met en place un routage MPLS où ceux-ci sont labelisés 5 (tous les autres flux ont un label à 0). 
- Mise en place de règles Iptables afin de trier les requêtes et de leur attribuer un label.
- Il suffit d'exécuter le script shell PEP.sh (dans le même répertoire que tcp_qos_server.py) qui va aussi exécuter le script python. 

# Fichiers de configuration des routeurs Cisco
- Mise en place des policy-map sur les routeurs Cisco avec différentes classes de traffic avec des priorités.



# éléments manquants (SECTION A SUPPRIMER UNE FOIS LES DERNIERS FICHIERS AJOUTES)
- attente de la version récente de tcp_qos_server.py, PEP.sh et des configurations des routeurs Cisco.
