/*
    Copyright (C) 2009 Nick Whitelegg, nick_whitelegg@yahoo.co.uk

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111 USA

 */
#include "SRTMRenderer.h"
#include <cstdio>
#include <cmath>
#include <iostream>
using namespace std;

SRTMRenderer::SRTMRenderer(SRTM *srtm)
{
    this->srtm = srtm;
    this->normals = NULL;
}

void SRTMRenderer::generateNormals(int hExag)
{
    int nrows=(srtm->bottom-srtm->top)+1;
    int ncols=(srtm->right-srtm->left)+1;
	if(this->normals==NULL)
        this->normals = new Vec[nrows*ncols];
    int i=0;
    Vec up,down,left,right,n[4];
    for(int row=0; row<nrows; row++)
    {
        for(int col=0; col<ncols; col++)
        {

            if(row<nrows-1)
            {
                down = Vec( 
                srtm->heights[i+ncols].pos.n-srtm->heights[i].pos.n,
                (srtm->heights[i+ncols].height-srtm->heights[i].height)*hExag,
                srtm->heights[i+ncols].pos.e-srtm->heights[i].pos.e
                      );
            }
            if(row>0)
            {
                up = Vec( 
                srtm->heights[i-ncols].pos.n-srtm->heights[i].pos.n,
                (srtm->heights[i-ncols].height-srtm->heights[i].height)*hExag,
                srtm->heights[i-ncols].pos.e-srtm->heights[i].pos.e
                      );
            }
            if(col<ncols-1)
            {
                right = Vec( 
                srtm->heights[i+1].pos.n-srtm->heights[i].pos.n,
                (srtm->heights[i+1].height-srtm->heights[i].height)*hExag,
                srtm->heights[i+1].pos.e-srtm->heights[i].pos.e
                      );
            }
            if(col>0)
            {
                left = Vec( 
                srtm->heights[i-1].pos.n-srtm->heights[i].pos.n,
                (srtm->heights[i-1].height-srtm->heights[i].height)*hExag,
                srtm->heights[i-1].pos.e-srtm->heights[i].pos.e
                      );
            }

            if(row<nrows-1 && col<ncols-1)
                n[0] = normal(down,right);
            if(row>0 && col<ncols-1)
                n[1] = normal(right,up);
            if(row>0 && col>0)
                n[2] = normal(up,left);
            if(row<nrows-1 && col>0)
                n[3] = normal(left,down);
            
             normals[i].x = n[0].x+n[1].x+n[2].x+n[3].x;
             normals[i].y = n[0].y+n[1].y+n[2].y+n[3].y;
             normals[i].z = n[0].z+n[1].z+n[2].z+n[3].z;
             normals[i].normalise();

             i++;
        }    
    }
}


void SRTMRenderer::render(int res, int hExag, GLfloat maxHt)
{
    if(!(srtm->isLoaded())) return;
    
    //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    //glBegin(GL_TRIANGLES);
    //glBegin(GL_QUADS);

    int i;

    glColor3f(0.0f,1.0f,0.0f);

    int nrows = (srtm->bottom-srtm->top)+1, ncols = (srtm->right-srtm->left)+1;
    for (int row=0; row<nrows-res; row+=res)
    {
		glBegin(GL_TRIANGLE_STRIP);
        for(int col=0; col<ncols-res; col+=res)
        {
            i = row*ncols+col;

            // anticlockwise
            // top left - bottom left - bottom right - top right
            // triangle: topleft-bottomleft-bottomright
            // triangle bottomright-topright-topleft

            glNormal3f(normals[i].x,normals[i].y,normals[i].z);
            
            glVertex3f(srtm->heights[i].pos.n,
                       srtm->heights[i].height*hExag,
                       srtm->heights[i].pos.e);

            
            glNormal3f(normals[i+ncols*res].x,
                        normals[i+ncols*res].y,
                        normals[i+ncols*res].z);
        

            glVertex3f(srtm->heights[i+ncols*res].pos.n,
                       srtm->heights[i+ncols*res].height*hExag,
                       srtm->heights[i+ncols*res].pos.e);



			/*
            glNormal3f(normals[i+ncols*res+res].x,
                        normals[i+ncols*res+res].y,
                        normals[i+ncols*res+res].z);

            glVertex3f(srtm->heights[i+ncols*res+res].pos.n,
                       srtm->heights[i+ncols*res+res].height*hExag,
                       srtm->heights[i+ncols*res+res].pos.e);

            glVertex3f(srtm->heights[i+ncols*res+res].pos.n,
                       srtm->heights[i+ncols*res+res].height*hExag,
                       srtm->heights[i+ncols*res+res].pos.e);

            glNormal3f(normals[i+res].x,normals[i+res].y,normals[i+res].z);
            
            glVertex3f(srtm->heights[i+res].pos.n,
                       srtm->heights[i+res].height*hExag,
                       srtm->heights[i+res].pos.e);

            glNormal3f(normals[i].x,normals[i].y,normals[i].z);
            
            glVertex3f(srtm->heights[i].pos.n,
                       srtm->heights[i].height*hExag,
                       srtm->heights[i].pos.e);
			*/
		}
		glEnd();
    }
    glColor3f(1.0f,1.0f,1.0f);
}
