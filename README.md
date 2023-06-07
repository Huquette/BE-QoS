# BE-QoS

Ce depot contient tous les documents modifiés ou créés dans le cadre du BE-QoS 2023. Le mode de fonctionnement des appels est explicité via le diagramme de sequence (diagramme seq à OUVRIR sur draw.io) avec notamment les informations échangées lors d'un INVITE comme les numéros de ports et @IP correctes. Slides de la présentation accessibles via le document BE QoS Propositon.pptx.

# Les fonctions modifiées/crées pour le proxy sont : processIncomingResponse (ligne 2327) et forwardToBandwidthBroker (ligne 2877). 
- Envoi du proxy au bandwidth broker la requete ou fin d'appel, qui vérifie la disponibilité du réseau (variable représentant la bandwidth totale disponible, mise à jour à chaque nouvel appel), et tient un compte des appels en cours via une ArrayList, afin de n'envoyer qu'une seule requete/fin de ressources au TraficController (le proxy forward toutes les INVITEs, hors il y en a plusieurs par appel). 
- Le proxy est à lancer sur la bonne machine (celle-ci utilisant les ports 5060 pour ses service de proxy ne peut plus être client). On peut lancer ces services depuis le binaire exécutable ( dans le répertoire workspace/openjsip/bin) : ./openjsip start all


# Fonction du BandwidthBroker qui verifie le type de requête et l'envoie au TC: processRequest
- Reception de la requete de liberation ou reservation par le TC, qui met en place un shaping des paquets sortants a chaque routeur linux d'entrée/sortie du réseau client (champs TOS = 46). Ce shaping est réalisé par l'execution d'un fichier python qui reçoit les requêtes du BB et écrits des commandes sous linux. 
- Le Bandwidth Broker est lancé depuis un des routeurs Linux. Afin de l'exécuter, il suffit de taper : java BandwidthBroker   (dans le répertoire où doit se trouver BandwidthBroker.java, ClientsConnection.java, Reseau.java)

# Fichier qos_tcp_server.py et PEP.sh - Policy Enforcement Points
- Script a executer au niveau de chacun des routeurs linux de bordure en ayant pris soin de modifier l'adresse ip du serveur python dans le fichier qos_tcp_server.py
- le script crée 2 queue HTB, une pour les requêtes de reservation de taille 512kbps et une pour le reste du traffic de taille 2Mbps
- le serveur python execute des commandes iptables en rentrée du routeur pour marqué les flux afin de les placer dans la bonne queue, et en sortie marquer leur champs DSCP a 46(Expedited Forwarding) afin de permettre leur reconnaissance et leur policing au niveau des interfaces d'entrée du réseau de coeur.
- Il execute l'opération inverse lors de la requête de libération de ressources.

# Fichiers de configuration des routeurs Cisco
- Details class-maps:

- class-map match-any TR (classe Temps Réel)
- match ip dscp ef (match le marquage DSCP fait au niveau des PEP)
- class-map match-all MPLS-TR (classe Temps Réel dans le nuage mpls)
- match mpls experimental topmost 5 (match la translation EF dans MPLS)
- class-map match-all MPLS-DEFAULT (classe défault)
- match mpls experimental topmost 0

- Details policy-maps:

- policy-map Police-Client (police d'entrée sur les interfaces VRF)
- class TR 
- police cir 512000 (ici on match la capacité de la file prio du PEP)
- conform-action transmit
- exceed-action drop
- violate-action drop
- class class-default
- police cir 2000000 (ici on match la capacité de la file default du PEP)
- conform-action transmit
- exceed-action drop
- violate-action drop

policy-map MPLS-PHB (police de sortie sur les interfaces côté nuage MPLS)
class MPLS-TR 
priority percent 30 (on priorise le trafic temps réel a hauteur de 30%)
class MPLS-DEFAULT
bandwidth percent 60 (on alloue 60% de la bande-passante au reste du trafic)

