/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.causalclustering.protocol.handshake;

import co.unruly.matchers.OptionalMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import org.neo4j.causalclustering.protocol.Protocol;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertThat;
import static org.neo4j.helpers.collection.Iterators.asSet;

public class ProtocolRepositoryTest
{
    private ProtocolRepository protocolRepository = new ProtocolRepository( TestProtocols.values() );

    @Test
    public void shouldReturnEmptyIfUnknownVersion() throws Throwable
    {
        // when
        Optional<Protocol> applicationProtocol = protocolRepository.select( TestProtocols.Identifier.RAFT.canonicalName(), -1 );

        // then
        assertThat( applicationProtocol, OptionalMatchers.empty() );
    }

    @Test
    public void shouldReturnEmptyIfUnknownName() throws Throwable
    {
        // when
        Optional<Protocol> applicationProtocol = protocolRepository.select( "not a real protocol", 1 );

        // then
        assertThat( applicationProtocol, OptionalMatchers.empty() );
    }

    @Test
    public void shouldReturnEmptyIfNoVersions() throws Throwable
    {
        // when
        Optional<Protocol> applicationProtocol = protocolRepository.select( TestProtocols.Identifier.RAFT.canonicalName(), emptySet());

        // then
        assertThat( applicationProtocol, OptionalMatchers.empty() );
    }

    @Test
    public void shouldReturnProtocolIfKnownNameAndVersion() throws Throwable
    {
        // when
        Optional<Protocol> applicationProtocol = protocolRepository.select( TestProtocols.Identifier.RAFT.canonicalName(), 1 );

        // then
        assertThat( applicationProtocol, OptionalMatchers.contains( TestProtocols.RAFT_1 ) );
    }

    @Test
    public void shouldReturnKnownProtocolVersionWhenFirstGivenVersionNotKnown() throws Throwable
    {
        // when
        Optional<Protocol> applicationProtocol = protocolRepository.select( TestProtocols.Identifier.RAFT.canonicalName(), asSet( -1, 1 ));

        // then
        assertThat( applicationProtocol, OptionalMatchers.contains( TestProtocols.RAFT_1 ) );
    }

    @Test
    public void shouldReturnProtocolOfHighestVersionRequestedAndSupported() throws Throwable
    {
        // when
        Optional<Protocol> applicationProtocol = protocolRepository.select( TestProtocols.Identifier.RAFT.canonicalName(), asSet( 9, 1, 3, 2, 7 ) );

        // then
        assertThat( applicationProtocol, OptionalMatchers.contains( TestProtocols.RAFT_3 ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotInstantiateIfDuplicateProtocolsSupplied() throws Throwable
    {
        // given
        Protocol protocol = new Protocol()
        {

            @Override
            public String identifier()
            {
                return "foo";
            }

            @Override
            public int version()
            {
                return 1;
            }
        };
        Protocol[] protocols = {protocol, protocol};

        // when
        new ProtocolRepository( protocols );

        // then throw
    }
}
