# BE-QoS

Ce depot contient tous les documents modifiés ou créés dans le cadre du BE-QoS 2023. Le mode de fonctionnement des appels est explicité via le diagramme de sequence (diagramme seq), et est comme suit:

- Envoi d'INVITE au Proxy java qui transmet à l'appelé, puis interception de la réponse de l'appelé contenant les numeros de ports et ip correctes. Les fonctions modifiées/crées sont : processIncomingResponse et forwardBandwidthBroker. 
- Envoi du proxy au bandwidth broker la requete ou fin d'appel, qui vérifie la disponibilité du réseau (variable représentant la bandwidth totale disponible, mise à jour à chaque nouvel appel), et tient un compte des appels en cours via une ArrayList, afin de n'envoyer qu'une seule requete/fin de ressources au TraficController (le proxy forward toutes les INVITEs, hors il y en a plusieurs par appel). 
- Reception de la requete de liberation ou reservation par le TC, qui met en place un shaping des paquets sortants a chaque routeur linux d'entrée/sortie du réseau client (champs TOS = 46). 
- Les paquets notés prioritaires par les routeurs du réseau client sont reconnus par le réseau de coeur qui met en place un routage MPLS où ceux-ci sont labelisés 5 (tous les autres flux ont un label à 0). 
