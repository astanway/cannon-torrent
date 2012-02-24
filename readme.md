Matthew Halupka 121004243
Abe Stanway 121005181
 
 
Our BitTorrent client works by first reading the torrent file and contacting the tracker and then taking the peerlist and finding the correct peer to connect to. After we have the correct peer we then perform the handshaking between our client and the peer. We verify the hash and proceed to see if we want what the peer has. Once we send the interested message we wait to be unchoked and then we begin the downloading process. We loop through what pieces, requesting each piece as multiple 16KB blocks. We adjust our logic for the last block which is less than 16KB. We store the incomplete piece in a buffer and wait for it to finish before writing it to the file in order to perform the piece hash check. Once all the pieces are complete, we finish the download, tell the tracker we are done and close our sockets.

We use three proprietary classes - RUBTClient, Peer and Helper. 

Helper class is a miscellaneous class with various methods we use for printing out byte arrays or verifying hashes against one another.
 
Peer is what we use to define a peer we might want to connect to. It consists of the peer id, the ip and port, and the input/output streams we are going to be working with. It interacts directly with the client class in that it is used to separate the preparation we need to do when we create a peer and when we open sockets and establish the streams from the socket. It also hold methods for sending messages to the defined peer such as requests or pieces.
 
The RUBTClient is our main class. It runs the entire thing and takes care of most of the work. It interacts with the peer class in that it calls the methods from the peer class in order to execute the download. It also uses the provided streams from the peer class in order to access data from the specific peer and get the blocks of pieces we are waiting for. It also calls the methods from Helpers in order to help with the verification of hashes.