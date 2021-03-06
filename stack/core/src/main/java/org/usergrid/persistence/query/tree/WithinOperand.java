/*******************************************************************************
 * Copyright 2012 Apigee Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.usergrid.persistence.query.tree;


import org.antlr.runtime.Token;


/** @author tnine */
public class WithinOperand extends Operand {

    /**
     * @param property
     * @param literal
     */
    public WithinOperand( Token t ) {
        super( t );
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.usergrid.persistence.query.tree.Operand#visit(org.usergrid.persistence
     * .query.tree.QueryVisitor)
     */
    @Override
    public void visit( QueryVisitor visitor ) {
        visitor.visit( this );
    }


    /**
     * @param propName
     */
    public void setProperty( String propName ) {
        setChild( 0, new WithinProperty( propName ) );
    }


    /**
     * @param distance
     */
    public void setDistance( float distance ) {
        setChild( 1, new FloatLiteral( distance ) );
    }


    /**
     * @param lattitude
     */
    public void setLattitude( float lattitude ) {
        setChild( 2, new FloatLiteral( lattitude ) );
    }


    /**
     * @param longitude
     */
    public void setLongitude( float longitude ) {
        setChild( 3, new FloatLiteral( longitude ) );
    }


    /**
     *
     * @return
     */
    public WithinProperty getProperty() {
        return ( WithinProperty ) this.children.get( 0 );
    }


    /**
     *
     * @return
     */
    public NumericLiteral getDistance() {
        return ( NumericLiteral ) this.children.get( 1 );
    }


    /**
     * @return
     */
    public NumericLiteral getLattitude() {
        return ( NumericLiteral ) this.children.get( 2 );
    }


    /**
     * @return
     */
    public NumericLiteral getLongitude() {
        return ( NumericLiteral ) this.children.get( 3 );
    }
}
