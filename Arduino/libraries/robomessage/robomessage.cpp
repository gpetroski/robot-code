class Command { 
	char type;
	char dir;
	float percent;
	int prox;
	char* message;
}

char* createMessageFromCommand(command cmd) {
	char message[20];
	switch (cmd.type) {
		case 'M':
			
	}
}