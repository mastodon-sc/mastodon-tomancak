package org.mastodon.mamut.tomancak.collaboration.sshauthentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;

/**
 * Example of how to generate a OpenSSH key pair and write it to stdout
 * with the apache sshd library.
 */
public class GenerateKeysExample
{

	public static void main( String[] args ) throws GeneralSecurityException, IOException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance( "RSA" );
		keyGen.initialize( 4 * 1024 );
		KeyPair keyPair = keyGen.genKeyPair();
		OpenSSHKeyPairResourceWriter.INSTANCE.writePrivateKey( keyPair, "user@example", new OpenSSHKeyEncryptionContext(), System.out );
		OpenSSHKeyPairResourceWriter.INSTANCE.writePublicKey( keyPair, "user@example", System.out );
	}
}
